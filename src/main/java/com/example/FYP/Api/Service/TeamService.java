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
    public TeamStatsDTO getStatistics(Long teamId, Long leagueId, Integer season) {
        try {
            // If both leagueId and season are provided, use them directly
            if (leagueId != null && leagueId > 0 && season != null && season > 0) {
                return getStatisticsForLeague(teamId, leagueId, season);
            }
            
            // If only leagueId is provided, use default season (current year)
            if (leagueId != null && leagueId > 0) {
                int defaultSeason = season != null && season > 0 ? season : 2024;
                return getStatisticsForLeague(teamId, leagueId, defaultSeason);
            }
            
            // Otherwise, auto-detect league and season
            return getStatisticsAuto(teamId, season);
        } catch (Exception e) {
            log.error("Error fetching statistics for team ID {}: {}", teamId, e.getMessage());
            return getDefaultStats();
        }
    }
    
    /**
     * Automatically determine league and season, then fetch statistics
     */
    private TeamStatsDTO getStatisticsAuto(Long teamId, Integer providedSeason) {
        try {
            // Step 1: Fetch team's current leagues
            String leaguesResponse = fetchFromApi("leagues", 
                    Map.of("team", teamId.toString(), "current", "true"));
            JsonNode leaguesRoot = objectMapper.readTree(leaguesResponse);
            JsonNode leaguesArray = leaguesRoot.path("response");
            
            if (!leaguesArray.isArray() || leaguesArray.size() == 0) {
                log.warn("No current leagues found for team ID: {}", teamId);
                return getDefaultStats();
            }
            
            // Step 2: Find the best league (prioritize major leagues)
            Long selectedLeagueId = null;
            Integer selectedSeason = null;
            
            // Priority order: Premier League (39), La Liga (140), Serie A (135), Bundesliga (78), Ligue 1 (61)
            int[] priorityLeagues = {39, 140, 135, 78, 61};
            
            // First, try to find a priority league
            for (int priorityId : priorityLeagues) {
                for (JsonNode league : leaguesArray) {
                    JsonNode leagueData = league.path("league");
                    if (leagueData.path("id").asInt(0) == priorityId) {
                        selectedLeagueId = (long) priorityId;
                        // Use provided season if available, otherwise extract from response
                        JsonNode seasons = leagueData.path("seasons");
                        if (providedSeason != null && providedSeason > 0) {
                            selectedSeason = providedSeason;
                        } else if (seasons.isArray() && seasons.size() > 0) {
                            // Get the current/active season (usually the last one in the array)
                            JsonNode lastSeason = seasons.get(seasons.size() - 1);
                            selectedSeason = lastSeason.path("year").asInt(2024);
                        } else {
                            selectedSeason = 2024;
                        }
                        log.info("Found priority league {} for team {}, season {}", selectedLeagueId, teamId, selectedSeason);
                        break;
                    }
                }
                if (selectedLeagueId != null) break;
            }
            
            // If no priority league found, use the first one
            if (selectedLeagueId == null) {
                JsonNode firstLeague = leaguesArray.get(0);
                JsonNode leagueData = firstLeague.path("league");
                selectedLeagueId = (long) leagueData.path("id").asInt(0);
                
                // Use provided season if available, otherwise extract from response
                JsonNode seasons = leagueData.path("seasons");
                if (providedSeason != null && providedSeason > 0) {
                    selectedSeason = providedSeason;
                } else if (seasons.isArray() && seasons.size() > 0) {
                    JsonNode lastSeason = seasons.get(seasons.size() - 1);
                    selectedSeason = lastSeason.path("year").asInt(2024);
                } else {
                    selectedSeason = 2024;
                }
                log.info("Using first available league {} for team {}, season {}", selectedLeagueId, teamId, selectedSeason);
            }
            
            if (selectedLeagueId == null || selectedLeagueId == 0) {
                log.warn("Could not determine league for team ID: {}", teamId);
                return getDefaultStats();
            }
            
            // Step 3: Fetch statistics using the determined league and season
            return getStatisticsForLeague(teamId, selectedLeagueId, selectedSeason);
            
        } catch (Exception e) {
            log.error("Error in auto-fetching statistics for team ID {}: {}", teamId, e.getMessage());
            return getDefaultStats();
        }
    }
    
    /**
     * Get team statistics for a specific league and season
     */
    private TeamStatsDTO getStatisticsForLeague(Long teamId, Long leagueId, Integer season) {
        try {
            // Fetch team statistics from external API
            String jsonResponse = fetchFromApi("teams/statistics", 
                    Map.of("team", teamId.toString(), "league", leagueId.toString(), "season", season.toString()));
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.path("response");
            
            if (!response.isArray() || response.size() == 0) {
                log.warn("No statistics response for team {} in league {} season {}", teamId, leagueId, season);
                return getDefaultStats();
            }

            // API-Sports returns: response[0].league.statistics (aggregated statistics object)
            JsonNode statistics = response.get(0).path("league").path("statistics");
            if (statistics.isMissingNode() || !statistics.isObject()) {
                log.warn("Statistics object not found in response for team {} in league {} season {}", teamId, leagueId, season);
                return getDefaultStats();
            }

            return parseStatistics(statistics);
        } catch (Exception e) {
            log.error("Error fetching statistics for team ID {} in league {} season {}: {}", 
                    teamId, leagueId, season, e.getMessage());
            return getDefaultStats();
        }
    }

    private TeamStatsDTO parseStatistics(JsonNode stats) {
        // Summary fields - fixtures.played.total
        JsonNode fixtures = stats.path("fixtures");
        Integer matchesPlayed = safeInt(fixtures.path("played").path("total"));
        Integer wins = safeInt(fixtures.path("wins").path("total"));
        Integer draws = safeInt(fixtures.path("draws").path("total"));
        Integer losses = safeInt(fixtures.path("loses").path("total"));
        
        // Goals - API structure: goals.for.total (sometimes nested as goals.for.total.total)
        JsonNode goals = stats.path("goals");
        JsonNode goalsFor = goals.path("for");
        JsonNode goalsAgainst = goals.path("against");
        
        // Try both paths: goals.for.total and goals.for.total.total
        Integer goalsScored = safeInt(goalsFor.path("total"));
        if (goalsScored == null && goalsFor.path("total").isObject()) {
            goalsScored = safeInt(goalsFor.path("total").path("total"));
        }
        
        // Goals per match - goals.for.average.total or goals.for.average
        String goalsPerMatch = null;
        JsonNode goalsForAverage = goalsFor.path("average");
        if (goalsForAverage.isObject()) {
            goalsPerMatch = safeString(goalsForAverage.path("total"));
        } else {
            goalsPerMatch = safeString(goalsForAverage);
        }
        
        Integer goalsConceded = safeInt(goalsAgainst.path("total"));
        if (goalsConceded == null && goalsAgainst.path("total").isObject()) {
            goalsConceded = safeInt(goalsAgainst.path("total").path("total"));
        }
        
        // Goals conceded per match - goals.against.average.total or goals.against.average
        String goalsConcededPerMatch = null;
        JsonNode goalsAgainstAverage = goalsAgainst.path("average");
        if (goalsAgainstAverage.isObject()) {
            goalsConcededPerMatch = safeString(goalsAgainstAverage.path("total"));
        } else {
            goalsConcededPerMatch = safeString(goalsAgainstAverage);
        }
        
        // Goal difference - calculate from goals scored and conceded
        Integer goalDifference = null;
        if (goalsScored != null && goalsConceded != null) {
            goalDifference = goalsScored - goalsConceded;
        }
        
        // Clean sheets - API structure: clean_sheet.total
        Integer cleanSheets = safeInt(stats.path("clean_sheet").path("total"));
        
        // Attacking - shots.total, shots.on.total, penalty.scored.total
        JsonNode shots = stats.path("shots");
        Integer shotsTotal = safeInt(shots.path("total"));
        Integer shotsOnTarget = safeInt(shots.path("on").path("total"));
        
        JsonNode penalty = stats.path("penalty");
        Integer penaltiesScored = safeInt(penalty.path("scored").path("total"));
        
        // Passing - passes.total, passes.accurate, passes.percentage
        JsonNode passes = stats.path("passes");
        Integer passesTotal = safeInt(passes.path("total"));
        Integer passesAccurate = safeInt(passes.path("accurate"));
        String passAccuracy = safeString(passes.path("percentage"));
        
        // Defending - tackles.total, tackles.interceptions, goalkeeper.saves
        JsonNode tackles = stats.path("tackles");
        Integer tacklesTotal = safeInt(tackles.path("total"));
        Integer interceptions = safeInt(tackles.path("interceptions"));
        
        JsonNode goalkeeper = stats.path("goalkeeper");
        Integer saves = safeInt(goalkeeper.path("saves"));
        
        // Cards - aggregate from cards map structure (time buckets like "0-15", "16-30", etc.)
        JsonNode cards = stats.path("cards");
        Integer yellowCards = aggregateCardCount(cards.path("yellow"));
        Integer redCards = aggregateCardCount(cards.path("red"));
        
        // Fouls - fouls.committed.total
        JsonNode foulsNode = stats.path("fouls");
        Integer fouls = safeInt(foulsNode.path("committed").path("total"));
        
        return TeamStatsDTO.builder()
                .matchesPlayed(matchesPlayed != null ? matchesPlayed : 0)
                .wins(wins != null ? wins : 0)
                .draws(draws != null ? draws : 0)
                .losses(losses != null ? losses : 0)
                .goalDifference(goalDifference != null ? goalDifference : 0)
                .cleanSheets(cleanSheets != null ? cleanSheets : 0)
                .goalsScored(goalsScored != null ? goalsScored : 0)
                .goalsPerMatch(goalsPerMatch != null ? goalsPerMatch : "0.00")
                .shots(shotsTotal != null ? shotsTotal : 0)
                .shotsOnTarget(shotsOnTarget != null ? shotsOnTarget : 0)
                .penaltiesScored(penaltiesScored != null ? penaltiesScored : 0)
                .passes(passesTotal != null ? passesTotal : 0)
                .passesAccurate(passesAccurate != null ? passesAccurate : 0)
                .passAccuracy(passAccuracy != null ? passAccuracy : "0%")
                .goalsConceded(goalsConceded != null ? goalsConceded : 0)
                .goalsConcededPerMatch(goalsConcededPerMatch != null ? goalsConcededPerMatch : "0.00")
                .tackles(tacklesTotal != null ? tacklesTotal : 0)
                .interceptions(interceptions != null ? interceptions : 0)
                .saves(saves != null ? saves : 0)
                .yellowCards(yellowCards != null ? yellowCards : 0)
                .redCards(redCards != null ? redCards : 0)
                .fouls(fouls != null ? fouls : 0)
                .build();
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

