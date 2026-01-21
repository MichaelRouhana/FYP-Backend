package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Mapper.FixtureMapper;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
import com.example.FYP.Api.Model.View.Team.*;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final FixtureRepository fixtureRepository;
    private final FootBallService footBallService;
    private final FixtureMapper fixtureMapper;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${football.api.key}")
    private String apiKey;


    public TeamHeaderDTO getTeamHeader(Long teamId) {
        try {
            String jsonResponse = fetchFromApi("teams", Map.of("id", teamId.toString()));
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.path("response");
            
            if (!response.isArray() || response.size() == 0) {
                throw new EntityNotFoundException("Team not found with ID: " + teamId);
            }

            JsonNode teamData = response.get(0).path("team");
            JsonNode venue = response.get(0).path("venue");
            JsonNode coach = response.get(0).path("coach");

            CoachDTO coachDTO = footBallService.getCoachByTeamId(teamId);
            
            String coachName = coachDTO != null && coachDTO.getName() != null && !coachDTO.getName().isEmpty()
                    ? coachDTO.getName()
                    : coach.path("name").asText("");
            
            String coachImageUrl = coachDTO != null && coachDTO.getPhotoUrl() != null && !coachDTO.getPhotoUrl().isEmpty()
                    ? coachDTO.getPhotoUrl()
                    : null;

            return TeamHeaderDTO.builder()
                    .name(teamData.path("name").asText(""))
                    .logo(teamData.path("logo").asText(""))
                    .foundedYear(teamData.path("founded").asInt(0))
                    .country(teamData.path("country").asText(""))
                    .stadiumName(venue.path("name").asText(""))
                    .coachName(coachName)
                    .coachImageUrl(coachImageUrl)
                    .uefaRanking(teamData.path("national").asBoolean(false) ? null : extractUefaRanking(teamData))
                    .build();
        } catch (Exception e) {
            log.error("Error fetching team header for ID {}: {}", teamId, e.getMessage());
            throw new EntityNotFoundException("Failed to fetch team header: " + e.getMessage());
        }
    }

    /**
     * Get the last finished match for a team
     */
    public FixtureViewDTO getLastMatch(Long teamId) {
        try {
            List<Fixture> fixtures = fixtureRepository.findLastFinishedMatchByTeamId(teamId);
            
            if (fixtures.isEmpty()) {
                return null;
            }

            Fixture lastFixture = fixtures.get(0);
            return fixtureMapper.toDTO(lastFixture);
        } catch (Exception e) {
            log.error("Error fetching last match for team ID {}: {}", teamId, e.getMessage());
            return null;
        }
    }

    /**
     * Get squad members for a team
     */
    public List<SquadMemberDTO> getSquad(Long teamId) {
        try {
            String jsonResponse = fetchFromApi("players/squads", Map.of("team", teamId.toString()));
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.path("response");
            
            if (!response.isArray() || response.size() == 0) {
                return Collections.emptyList();
            }

            JsonNode players = response.get(0).path("players");
            if (!players.isArray() || players.size() == 0) {
                return Collections.emptyList();
            }

            List<SquadMemberDTO> squad = new ArrayList<>();
            for (JsonNode player : players) {
                String birthDateStr = player.path("birth").path("date").asText("");
                String contractUntilStr = player.path("contract").path("until").asText("");
                
                SquadMemberDTO member = SquadMemberDTO.builder()
                        .id(player.path("id").asLong(0))
                        .name(player.path("name").asText(""))
                        .photoUrl(player.path("photo").asText(""))
                        .position(mapPositionToCategory(player.path("position").asText("")))
                        .age(calculateAge(birthDateStr))
                        .height(parseHeight(player.path("height").asText("")))
                        .marketValue(null) // Not available in standard API
                        .contractUntil(parseDate(contractUntilStr))
                        .previousClub(null) // Not available in standard API
                        .build();
                squad.add(member);
            }

            return squad.stream()
                    .sorted(Comparator.comparing(m -> getPositionOrder(m.getPosition())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching squad for team ID {}: {}", teamId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get team statistics for a specific league and season
     * @param teamId The team ID
     * @param leagueId The league ID
     * @param season The season year (e.g., 2024)
     * @return TeamStatsDTO with categorized statistics
     */
    public TeamStatsDTO getTeamStatistics(Long teamId, Long leagueId, Integer season) {
        try {
            if (season == null) {
                season = 2025;
            }
            
            String jsonResponse = fetchFromApi("teams/statistics", 
                    Map.of("team", teamId.toString(), "league", leagueId.toString(), "season", season.toString()));
            
            log.info("API Response for team {} league {} season {}: {}", teamId, leagueId, season, jsonResponse);
            
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.path("response");
            
            if (root.has("errors") && root.path("errors").size() > 0) {
                log.error("API returned errors: {}", root.path("errors"));
                return buildDefaultStats();
            }
            
            if (response.isMissingNode() || response.isNull()) {
                log.warn("Missing response for team {} league {} season {}", teamId, leagueId, season);
                return buildDefaultStats();
            }

            JsonNode statsData = response;
            
            JsonNode fixtures = statsData.path("fixtures");
            TeamStatsDTO.Summary summary = TeamStatsDTO.Summary.builder()
                    .played(safeInt(fixtures.path("played").path("total")))
                    .wins(safeInt(fixtures.path("wins").path("total")))
                    .draws(safeInt(fixtures.path("draws").path("total")))
                    .loses(safeInt(fixtures.path("loses").path("total")))
                    .form(safeString(statsData.path("form")))
                    .build();

            JsonNode goals = statsData.path("goals");
            JsonNode penalty = statsData.path("penalty");
            
            TeamStatsDTO.Attacking attacking = TeamStatsDTO.Attacking.builder()
                    .goalsScored(safeInt(goals.path("for").path("total").path("total")))
                    .penaltiesScored(safeInt(penalty.path("scored").path("total")))
                    .penaltiesMissed(safeInt(penalty.path("missed").path("total")))
                    .build();

            TeamStatsDTO.Defending defending = TeamStatsDTO.Defending.builder()
                    .goalsConceded(safeInt(goals.path("against").path("total").path("total")))
                    .cleanSheets(safeInt(statsData.path("clean_sheet").path("total")))
                            .build();

            JsonNode cards = statsData.path("cards");
            
            Integer yellowCards = 0;
            Integer redCards = 0;
            if (!cards.isMissingNode()) {
                JsonNode yellow = cards.path("yellow");
                if (yellow.has("total")) {
                    yellowCards = safeInt(yellow.path("total"));
                } else if (yellow.isObject()) {
                    yellowCards = yellow.findValues("total").stream()
                            .filter(JsonNode::isInt)
                            .mapToInt(JsonNode::asInt)
                            .sum();
                }
                
                JsonNode red = cards.path("red");
                if (red.has("total")) {
                    redCards = safeInt(red.path("total"));
                } else if (red.isObject()) {
                    redCards = red.findValues("total").stream()
                            .filter(JsonNode::isInt)
                            .mapToInt(JsonNode::asInt)
                            .sum();
                }
            }
            
            TeamStatsDTO.Other other = TeamStatsDTO.Other.builder()
                    .yellowCards(yellowCards)
                    .redCards(redCards)
                    .build();

            return TeamStatsDTO.builder()
                    .summary(summary)
                    .attacking(attacking)
                    .defending(defending)
                    .other(other)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching statistics for team ID {} in league {} season {}: {}", 
                    teamId, leagueId, season, e.getMessage());
            return buildDefaultStats();
        }
    }

    /**
     * Get team statistics for a specific league (backward compatibility)
     * Uses current season by default
     */
    public TeamStatsDTO getStatistics(Long teamId, Long leagueId) {
        return getTeamStatistics(teamId, leagueId, null);
    }

    /**
     * Get mock team statistics for testing UI when API credits are exhausted
     * Returns dummy data to test the frontend display
     */
    public TeamStatsDTO getMockTeamStatistics(Long teamId) {
        return TeamStatsDTO.builder()
                .summary(TeamStatsDTO.Summary.builder()
                        .played(20)
                        .wins(10)
                        .draws(5)
                        .loses(5)
                        .form("WDLWW")
                        .build())
                .attacking(TeamStatsDTO.Attacking.builder()
                        .goalsScored(35)
                        .penaltiesScored(3)
                        .penaltiesMissed(1)
                        .build())
                .defending(TeamStatsDTO.Defending.builder()
                        .goalsConceded(20)
                        .cleanSheets(8)
                        .build())
                .other(TeamStatsDTO.Other.builder()
                        .yellowCards(40)
                        .redCards(2)
                        .build())
                .build();
        }

    private TeamStatsDTO buildDefaultStats() {
        return TeamStatsDTO.builder()
                .summary(TeamStatsDTO.Summary.builder()
                        .played(0)
                        .wins(0)
                        .draws(0)
                        .loses(0)
                        .form("")
                        .build())
                .attacking(TeamStatsDTO.Attacking.builder()
                        .goalsScored(0)
                        .penaltiesScored(0)
                        .penaltiesMissed(0)
                        .build())
                .defending(TeamStatsDTO.Defending.builder()
                        .goalsConceded(0)
                        .cleanSheets(0)
                        .build())
                .other(TeamStatsDTO.Other.builder()
                        .yellowCards(0)
                        .redCards(0)
                        .build())
                .build();
    }

    private Integer safeInt(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return 0;
        if (node.isInt()) return node.asInt();
        if (node.isTextual()) {
            String text = node.asText("");
            if (text.isEmpty()) return 0;
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private String safeString(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return "";
        String text = node.asText("");
        return text != null ? text : "";
    }

    /**
     * Get team details including stadium information
     */
    public TeamDetailsDTO getTeamDetails(Long teamId) {
        try {
            FootBallService.TeamResponse teamResponse = footBallService.getTeamInfo(teamId);
            
            if (teamResponse == null) {
                log.warn("No team info response for team ID: {}", teamId);
                throw new EntityNotFoundException("Team not found with ID: " + teamId);
            }

            FootBallService.TeamData teamData = teamResponse.getTeam();
            FootBallService.Venue venue = teamResponse.getVenue();

            if (teamData == null || venue == null) {
                throw new EntityNotFoundException("Team data incomplete for ID: " + teamId);
            }

            return TeamDetailsDTO.builder()
                    .stadiumName(venue.getName() != null ? venue.getName() : "")
                    .stadiumImage(venue.getImage() != null ? venue.getImage() : "")
                    .city(venue.getCity() != null ? venue.getCity() : "")
                    .capacity(venue.getCapacity())
                    .foundedYear(teamData.getFounded())
                    .build();
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching team details for ID {}: {}", teamId, e.getMessage());
            throw new EntityNotFoundException("Failed to fetch team details: " + e.getMessage());
        }
    }

    /**
     * Get trophies/honors for a team
     * Note: The API-Sports API may not have a dedicated trophies endpoint.
     * This method attempts to fetch trophies, but returns an empty list if not available.
     */
    public List<TrophyDTO> getTrophies(Long teamId) {
        try {
            String jsonResponse = fetchFromApi("trophies", Map.of("team", teamId.toString()));
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.path("response");
            
            if (!response.isArray() || response.size() == 0) {
                return Collections.emptyList();
            }

            List<TrophyDTO> trophies = new ArrayList<>();
            for (JsonNode trophy : response) {
                TrophyDTO dto = TrophyDTO.builder()
                        .leagueName(trophy.path("league").asText(""))
                        .season(trophy.path("season").asInt(0))
                        .isMajor(trophy.path("isMajor").asBoolean(false))
                        .build();
                trophies.add(dto);
            }

            return trophies;
        } catch (Exception e) {
            log.debug("Trophies endpoint not available for team ID {} (this is expected if not in API plan): {}", 
                    teamId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String fetchFromApi(String endpoint, Map<String, String> params) {
        String baseUrl = "https://v3.football.api-sports.io/" + endpoint;
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        
        if (!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1); // Remove last &
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.debug("Calling API: {}", urlBuilder.toString());
        ResponseEntity<String> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.GET,
                entity,
                String.class
        );
            
            if (response.getStatusCode().isError()) {
                log.error("API returned error status: {} - Body: {}", response.getStatusCode(), response.getBody());
            }

        return response.getBody();
        } catch (Exception e) {
            log.error("Error calling API endpoint {}: {}", urlBuilder.toString(), e.getMessage(), e);
            throw e;
        }
    }

    private Integer extractUefaRanking(JsonNode teamData) {
        if (teamData.has("rank")) {
            return teamData.path("rank").asInt(0);
        }
        return null;
    }

    private String mapPositionToCategory(String position) {
        if (position == null || position.isEmpty()) return "MID";
        String pos = position.toUpperCase();
        if (pos.contains("GOALKEEPER") || pos.equals("GK")) return "GK";
        if (pos.contains("DEFENDER") || pos.contains("BACK") || pos.equals("DEF")) return "DEF";
        if (pos.contains("FORWARD") || pos.contains("ATTACKER") || pos.equals("FWD")) return "FWD";
        return "MID"; // Default to midfielder
    }

    private Integer calculateAge(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.isEmpty()) return null;
        try {
            LocalDate birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseHeight(String heightStr) {
        if (heightStr == null || heightStr.isEmpty()) return null;
        try {
            String numeric = heightStr.replaceAll("[^0-9]", "");
            return numeric.isEmpty() ? null : Integer.parseInt(numeric);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private int getPositionOrder(String position) {
        return switch (position) {
            case "GK" -> 1;
            case "DEF" -> 2;
            case "MID" -> 3;
            case "FWD" -> 4;
            default -> 5;
        };
    }
}

