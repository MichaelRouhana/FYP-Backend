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
    private final String apiKey = "7aa48d98c402dc071bb8405ebbb722ec";

    /**
     * Get team header information
     */
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

            return TeamHeaderDTO.builder()
                    .name(teamData.path("name").asText(""))
                    .logo(teamData.path("logo").asText(""))
                    .foundedYear(teamData.path("founded").asInt(0))
                    .country(teamData.path("country").asText(""))
                    .stadiumName(venue.path("name").asText(""))
                    .coachName(coach.path("name").asText(""))
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
            // Find last finished match where team is either home or away
            List<Fixture> fixtures = fixtureRepository.findLastFinishedMatchByTeamId(teamId);
            
            if (fixtures.isEmpty()) {
                return null; // No finished matches found
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

            // API-Sports returns: response[0].players[] array
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

            // Sort by position: GK, DEF, MID, FWD
            return squad.stream()
                    .sorted(Comparator.comparing(m -> getPositionOrder(m.getPosition())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching squad for team ID {}: {}", teamId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get team statistics for a specific league
     */
    public TeamStatsDTO getStatistics(Long teamId, Long leagueId) {
        try {
            // Fetch team statistics from external API
            String jsonResponse = fetchFromApi("teams/statistics", 
                    Map.of("team", teamId.toString(), "league", leagueId.toString(), "season", "2024"));
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.path("response");
            
            if (!response.isArray() || response.size() == 0) {
                // Return default stats if API doesn't have data
                return TeamStatsDTO.builder()
                        .matchesPlayed(0)
                        .goalsScored(0)
                        .goalsPerGame(0.0)
                        .cleanSheets(0)
                        .yellowCards(0)
                        .redCards(0)
                        .build();
            }

            // API-Sports returns statistics in a specific format
            JsonNode leagueStats = response.get(0).path("league").path("statistics");
            if (leagueStats.isMissingNode() || !leagueStats.isArray() || leagueStats.size() == 0) {
                // Try alternative path: response[0].statistics
                JsonNode altStats = response.get(0).path("statistics");
                if (altStats.isMissingNode() || !altStats.isArray() || altStats.size() == 0) {
                    return TeamStatsDTO.builder()
                            .matchesPlayed(0)
                            .goalsScored(0)
                            .goalsPerGame(0.0)
                            .cleanSheets(0)
                            .yellowCards(0)
                            .redCards(0)
                            .build();
                }
                return parseStatisticsFromArray(altStats);
            }

            return parseStatisticsFromArray(leagueStats);
        } catch (Exception e) {
            log.error("Error fetching statistics for team ID {} in league {}: {}", teamId, leagueId, e.getMessage());
            // Return default stats on error
            return TeamStatsDTO.builder()
                    .matchesPlayed(0)
                    .goalsScored(0)
                    .goalsPerGame(0.0)
                    .cleanSheets(0)
                    .yellowCards(0)
                    .redCards(0)
                    .build();
        }
    }

    private TeamStatsDTO parseStatisticsFromArray(JsonNode statsArray) {
        // Aggregate statistics from all fixtures in the league
        int matchesPlayed = 0;
        int goalsScored = 0;
        int cleanSheets = 0;
        int yellowCards = 0;
        int redCards = 0;

        for (JsonNode matchStat : statsArray) {
            matchesPlayed++;
            JsonNode goals = matchStat.path("goals");
            goalsScored += goals.path("for").path("total").asInt(0);
            
            // Clean sheet: goals against = 0
            if (goals.path("against").path("total").asInt(0) == 0) {
                cleanSheets++;
            }

            JsonNode cards = matchStat.path("cards");
            yellowCards += cards.path("yellow").asInt(0);
            redCards += cards.path("red").asInt(0);
        }

        double goalsPerGame = matchesPlayed > 0 ? (double) goalsScored / matchesPlayed : 0.0;

        return TeamStatsDTO.builder()
                .matchesPlayed(matchesPlayed)
                .goalsScored(goalsScored)
                .goalsPerGame(Math.round(goalsPerGame * 100.0) / 100.0)
                .cleanSheets(cleanSheets)
                .yellowCards(yellowCards)
                .redCards(redCards)
                .build();
    }

    /**
     * Get trophies/honors for a team
     * Note: The API-Sports API may not have a dedicated trophies endpoint.
     * This method attempts to fetch trophies, but returns an empty list if not available.
     */
    public List<TrophyDTO> getTrophies(Long teamId) {
        try {
            // Attempt to fetch trophies from external API
            // Note: This endpoint may not exist in the standard API-Sports plan
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
            // Trophies endpoint might not exist in the API plan, return empty list gracefully
            log.debug("Trophies endpoint not available for team ID {} (this is expected if not in API plan): {}", 
                    teamId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Helper methods

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

        ResponseEntity<String> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }

    private Integer extractUefaRanking(JsonNode teamData) {
        // Try to extract ranking from various possible fields
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
            // Extract number from strings like "180 cm" or "180"
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

