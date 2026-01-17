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

            // Fetch coach details from dedicated coach endpoint
            CoachDTO coachDTO = footBallService.getCoachByTeamId(teamId);
            
            // Use coach from dedicated endpoint if available, otherwise fallback to team data
            String coachName = coachDTO != null && coachDTO.getName() != null && !coachDTO.getName().isEmpty()
                    ? coachDTO.getName()
                    : coach.path("name").asText("");
            
            String coachImageUrl = coachDTO != null && coachDTO.getPhotoUrl() != null && !coachDTO.getPhotoUrl().isEmpty()
                    ? coachDTO.getPhotoUrl()
                    : null; // null means no image, frontend will show icon fallback

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
     * Get team statistics - automatically determines league and season if not provided
     */
    public TeamStatsDTO getTeamStats(Long teamId, Long leagueId, Long season) {
        try {
            // 1. AUTO-DISCOVERY: If params are missing, find the active major league
            if (leagueId == null || season == null) {
                String leaguesJson = fetchFromApi("leagues", Map.of("team", teamId.toString(), "current", "true"));
                JsonNode leaguesResponse = objectMapper.readTree(leaguesJson).path("response");
                
                if (leaguesResponse.isArray() && leaguesResponse.size() > 0) {
                    JsonNode selectedLeague = leaguesResponse.get(0); // Default to first
                    
                    // Prioritize Major Leagues (PL, La Liga, Serie A, Bundesliga, Ligue 1)
                    for (JsonNode item : leaguesResponse) {
                        int id = item.path("league").path("id").asInt(0);
                        if (id == 39 || id == 140 || id == 135 || id == 78 || id == 61) {
                            selectedLeague = item;
                            break;
                        }
                    }
                    
                    leagueId = selectedLeague.path("league").path("id").asLong();
                    JsonNode seasons = selectedLeague.path("league").path("seasons");
                    if (seasons.isArray() && seasons.size() > 0) {
                        season = seasons.get(0).path("year").asLong();
                    } else {
                        season = 2024L;
                    }
                    log.info("Auto-Selected League: {} Season: {}", leagueId, season);
                } else {
                    return new TeamStatsDTO(); // Return empty if no leagues found
                }
            }

            // 2. FETCH STATS
            String statsJson = fetchFromApi("teams/statistics", Map.of(
                "team", teamId.toString(), 
                "league", leagueId.toString(), 
                "season", season.toString()
            ));
            
            JsonNode root = objectMapper.readTree(statsJson);
            JsonNode response = root.path("response");

            if (!response.isArray() || response.size() == 0) {
                log.warn("No statistics response for team {} in league {} season {}", teamId, leagueId, season);
                return new TeamStatsDTO();
            }

            // API-Football returns: response[0].league.statistics
            JsonNode stats = response.get(0).path("league").path("statistics");
            if (stats.isMissingNode() || !stats.isObject()) {
                log.warn("Statistics object not found in response for team {} in league {} season {}", teamId, leagueId, season);
                return new TeamStatsDTO();
            }

            // 3. MANUAL MAPPING (Prevents 0s)
            JsonNode fixtures = stats.path("fixtures");
            JsonNode goals = stats.path("goals");
            
            TeamStatsDTO dto = new TeamStatsDTO();
            
            // Summary
            dto.setMatchesPlayed(fixtures.path("played").path("total").asInt(0));
            dto.setWins(fixtures.path("wins").path("total").asInt(0));
            dto.setDraws(fixtures.path("draws").path("total").asInt(0));
            dto.setLosses(fixtures.path("loses").path("total").asInt(0));
            dto.setCleanSheets(stats.path("clean_sheet").path("total").asInt(0));
            
            // Goals - handle nested structure
            int scored = 0;
            JsonNode goalsForTotal = goals.path("for").path("total");
            if (goalsForTotal.isObject()) {
                scored = goalsForTotal.path("total").asInt(0);
            } else {
                scored = goalsForTotal.asInt(0);
            }
            
            int conceded = 0;
            JsonNode goalsAgainstTotal = goals.path("against").path("total");
            if (goalsAgainstTotal.isObject()) {
                conceded = goalsAgainstTotal.path("total").asInt(0);
            } else {
                conceded = goalsAgainstTotal.asInt(0);
            }
            
            dto.setGoalDifference(scored - conceded);

            // Attacking
            dto.setGoalsScored(scored);
            
            JsonNode goalsForAverage = goals.path("for").path("average");
            String goalsPerMatchStr = "0.0";
            if (goalsForAverage.isObject()) {
                goalsPerMatchStr = goalsForAverage.path("total").asText("0.0");
            } else {
                goalsPerMatchStr = goalsForAverage.asText("0.0");
            }
            dto.setGoalsPerMatch(goalsPerMatchStr);
            
            dto.setShots(stats.path("shots").path("total").asInt(0));
            dto.setShotsOnTarget(stats.path("shots").path("on").path("total").asInt(0));
            dto.setPenaltiesScored(stats.path("penalty").path("scored").path("total").asInt(0));

            // Passing
            dto.setPasses(stats.path("passes").path("total").asInt(0));
            dto.setPassesAccurate(stats.path("passes").path("accurate").asInt(0));
            dto.setPassAccuracy(stats.path("passes").path("percentage").asText("0%"));

            // Defending
            dto.setGoalsConceded(conceded);
            
            JsonNode goalsAgainstAverage = goals.path("against").path("average");
            String goalsConcededPerMatchStr = "0.0";
            if (goalsAgainstAverage.isObject()) {
                goalsConcededPerMatchStr = goalsAgainstAverage.path("total").asText("0.0");
            } else {
                goalsConcededPerMatchStr = goalsAgainstAverage.asText("0.0");
            }
            dto.setGoalsConcededPerMatch(goalsConcededPerMatchStr);
            
            dto.setTackles(stats.path("tackles").path("total").asInt(0));
            dto.setInterceptions(stats.path("tackles").path("interceptions").asInt(0));
            dto.setSaves(stats.path("goalkeeper").path("saves").asInt(0));
            
            // Cards (Summing up the map)
            int yellow = 0;
            JsonNode yellowMap = stats.path("cards").path("yellow");
            if (yellowMap.isObject()) {
                Iterator<String> fieldNames = yellowMap.fieldNames();
                while(fieldNames.hasNext()) {
                    yellow += yellowMap.path(fieldNames.next()).path("total").asInt(0);
                }
            }
            dto.setYellowCards(yellow);
            
            int red = 0;
            JsonNode redMap = stats.path("cards").path("red");
            if (redMap.isObject()) {
                Iterator<String> fieldNames = redMap.fieldNames();
                while(fieldNames.hasNext()) {
                    red += redMap.path(fieldNames.next()).path("total").asInt(0);
                }
            }
            dto.setRedCards(red);
            
            // Fouls
            dto.setFouls(stats.path("fouls").path("committed").path("total").asInt(0));

            return dto;

        } catch (Exception e) {
            log.error("Error fetching team stats for team ID {}: {}", teamId, e.getMessage(), e);
            return new TeamStatsDTO();
        }
    }
    
    private Integer safeInt(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        if (node.isInt()) return node.asInt();
        if (node.isTextual()) {
            String text = node.asText("");
            if (text.isEmpty()) return null;
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private String safeString(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        String text = node.asText("");
        return text.isEmpty() ? null : text;
    }
    
    /**
     * Aggregate card counts from a map structure like:
     * "yellow": {
     *   "0-15": {"total": 5},
     *   "16-30": {"total": 3},
     *   ...
     * }
     * Iterates through all time-bucket keys and sums up the totals.
     */
    private Integer aggregateCardCount(JsonNode cardMap) {
        if (cardMap.isMissingNode() || cardMap.isNull()) {
            return 0;
        }
        
        if (cardMap.isObject()) {
            int total = 0;
            Iterator<String> fieldNames = cardMap.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode cardNode = cardMap.path(fieldName);
                if (cardNode.isObject()) {
                    // Extract total from each time bucket (e.g., "0-15", "16-30")
                    total += cardNode.path("total").asInt(0);
                }
            }
            return total > 0 ? total : 0;
        }
        
        // If it's already an integer, return it directly
        if (cardMap.isInt()) {
            return cardMap.asInt();
        }
        
        return 0;
    }
    
    private TeamStatsDTO getDefaultStats() {
        return TeamStatsDTO.builder()
                .matchesPlayed(0)
                .wins(0)
                .draws(0)
                .losses(0)
                .goalDifference(0)
                .cleanSheets(0)
                .goalsScored(0)
                .goalsPerMatch("0.00")
                .shots(0)
                .shotsOnTarget(0)
                .penaltiesScored(0)
                .passes(0)
                .passesAccurate(0)
                .passAccuracy("0%")
                .goalsConceded(0)
                .goalsConcededPerMatch("0.00")
                .tackles(0)
                .interceptions(0)
                .saves(0)
                .yellowCards(0)
                .redCards(0)
                .fouls(0)
                .build();
    }

    /**
     * Get team details including stadium information
     */
    public TeamDetailsDTO getTeamDetails(Long teamId) {
        try {
            // Fetch team info from FootballService
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

            // Map the external data to TeamDetailsDTO
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

