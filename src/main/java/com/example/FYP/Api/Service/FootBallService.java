package com.example.FYP.Api.Service;

import com.example.FYP.Api.Model.View.Team.CoachDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class FootBallService {

    private String apiKey = "7aa48d98c402dc071bb8405ebbb722ec";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getLiveFixtures() {
        System.out.println(apiKey);
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://v3.football.api-sports.io/fixtures?live=all";

        HttpHeaders headers = new HttpHeaders();
        headers.put("x-apisports-key", Collections.singletonList(apiKey));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        log.info("Response: {}", response.getBody());
        return response.getBody();
    }

    public String getFixtures() {
        System.out.println(apiKey);
        RestTemplate restTemplate = new RestTemplate();

        String from = LocalDate.now().toString();

        String to = LocalDate.now().plusDays(30).toString();

        String url = "https://v3.football.api-sports.io/fixtures?from=" + from + "&to=" + to;

        HttpHeaders headers = new HttpHeaders();
        headers.put("x-apisports-key", Collections.singletonList(apiKey));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        log.info("Response: {}", response.getBody());
        return response.getBody();
    }


public String getFixturesByDate(String date) {
        // FIXED: Removed "&league=39" so we get ALL leagues (Egyptian, Spanish, etc.)
        // This ensures postponed matches in other leagues get updated.
        String url = "https://v3.football.api-sports.io/fixtures?date=" + date;

        HttpHeaders headers = new HttpHeaders();
        headers.put("x-apisports-key", Collections.singletonList(apiKey));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            // This log will confirm we are getting results!
            log.info("API Response for {}: {}", date, response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching fixtures for date {}: {}", date, e.getMessage());
            return null;
        }
    }

    /**
     * Fetches a single fixture by its ID from the Football API.
     * This is used to get the latest status for a specific match.
     */
    public String getFixtureById(Long fixtureId) {
        String url = "https://v3.football.api-sports.io/fixtures?id=" + fixtureId;

        HttpHeaders headers = new HttpHeaders();
        headers.put("x-apisports-key", Collections.singletonList(apiKey));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        log.debug("Fetched fixture by ID: {}", fixtureId);
        return response.getBody();
    }

    /**
     * Fetches the current active head coach for a specific team from the Football API.
     * 
     * The API returns a history of coaches. This method finds the coach with an active
     * career entry (where end is null) for the requested team.
     * 
     * @param teamId The team ID
     * @return CoachDTO with coach information, or null if no active coach found or API call fails
     */
    public CoachDTO getCoachByTeamId(Long teamId) {
        try {
            String url = "https://v3.football.api-sports.io/coachs?team=" + teamId;

            HttpHeaders headers = new HttpHeaders();
            headers.put("x-apisports-key", Collections.singletonList(apiKey));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getBody() == null) {
                log.warn("Empty response from coach API for team ID: {}", teamId);
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode responseArray = root.path("response");

            // Check if response is an array and has at least one element
            if (!responseArray.isArray() || responseArray.size() == 0) {
                log.debug("No coach found for team ID: {}", teamId);
                return null;
            }

            // Strategy 1: Find the coach with an active career entry (end is null)
            JsonNode activeCoach = findActiveCoach(responseArray, teamId);
            
            if (activeCoach != null) {
                log.debug("Found active coach for team ID: {}", teamId);
                return mapToCoachDTO(activeCoach);
            }

            // Strategy 2: Fallback - Find the most recent coach by checking career start dates
            JsonNode mostRecentCoach = findMostRecentCoach(responseArray, teamId);
            
            if (mostRecentCoach != null) {
                log.debug("Found most recent coach (fallback) for team ID: {}", teamId);
                return mapToCoachDTO(mostRecentCoach);
            }

            // Strategy 3: Last resort - Return the last coach in the list (often the newest)
            JsonNode lastCoach = responseArray.get(responseArray.size() - 1);
            log.debug("Using last coach in list (fallback) for team ID: {}", teamId);
            return mapToCoachDTO(lastCoach);

        } catch (Exception e) {
            log.error("Error fetching coach for team ID {}: {}", teamId, e.getMessage());
            return null; // Return null on error, allowing fallback handling
        }
    }

    /**
     * Finds the coach with an active career entry for the specified team.
     * An active career entry has end = null, meaning the coach is currently working.
     */
    private JsonNode findActiveCoach(JsonNode coachesArray, Long teamId) {
        for (JsonNode coach : coachesArray) {
            JsonNode career = coach.path("career");
            
            if (!career.isArray()) {
                continue;
            }

            // Check each career entry
            for (JsonNode careerEntry : career) {
                JsonNode team = careerEntry.path("team");
                Long careerTeamId = team.path("id").asLong(0);
                
                // Check if this career entry is for the requested team
                if (careerTeamId.equals(teamId)) {
                    // Check if the career entry is active (end is null or empty)
                    String endDate = careerEntry.path("end").asText(null);
                    if (endDate == null || endDate.isEmpty() || "null".equalsIgnoreCase(endDate)) {
                        return coach; // Found active coach
                    }
                }
            }
        }
        return null; // No active coach found
    }

    /**
     * Finds the most recent coach by checking career start dates.
     * Returns the coach with the latest start date for the specified team.
     */
    private JsonNode findMostRecentCoach(JsonNode coachesArray, Long teamId) {
        JsonNode mostRecentCoach = null;
        String mostRecentStartDate = null;

        for (JsonNode coach : coachesArray) {
            JsonNode career = coach.path("career");
            
            if (!career.isArray()) {
                continue;
            }

            // Check each career entry
            for (JsonNode careerEntry : career) {
                JsonNode team = careerEntry.path("team");
                Long careerTeamId = team.path("id").asLong(0);
                
                // Check if this career entry is for the requested team
                if (careerTeamId.equals(teamId)) {
                    String startDate = careerEntry.path("start").asText(null);
                    
                    // If this is the first match or has a more recent start date
                    if (startDate != null && !startDate.isEmpty() && !"null".equalsIgnoreCase(startDate)) {
                        if (mostRecentStartDate == null || startDate.compareTo(mostRecentStartDate) > 0) {
                            mostRecentStartDate = startDate;
                            mostRecentCoach = coach;
                        }
                    }
                }
            }
        }
        
        return mostRecentCoach;
    }

    /**
     * Maps a JsonNode coach object to CoachDTO.
     */
    private CoachDTO mapToCoachDTO(JsonNode coachData) {
        return CoachDTO.builder()
                .name(coachData.path("name").asText(""))
                .photoUrl(coachData.path("photo").asText(""))
                .nationality(coachData.path("nationality").asText(""))
                .build();
    }

    /**
     * Fetches team information including team and venue details from the Football API.
     * 
     * @param teamId The team ID
     * @return JSON response string containing team and venue information, or null if API call fails
     */
    public String getTeamInfo(Long teamId) {
        try {
            String url = "https://v3.football.api-sports.io/teams?id=" + teamId;

            HttpHeaders headers = new HttpHeaders();
            headers.put("x-apisports-key", Collections.singletonList(apiKey));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getBody() == null) {
                log.warn("Empty response from team info API for team ID: {}", teamId);
                return null;
            }

            log.debug("Fetched team info for team ID: {}", teamId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching team info for team ID {}: {}", teamId, e.getMessage());
            return null;
        }
    }
}
