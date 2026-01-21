package com.example.FYP.Api.Service;

import com.example.FYP.Api.Model.View.Team.CoachDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class FootBallService {


    @Value("${football.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getLiveFixtures() {
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

        return response.getBody();
    }

    public String getFixtures() {
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

        return response.getBody();
    }

    public String getFixturesByDate(String date) {
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
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching fixtures for date {}: {}", date, e.getMessage());
            return null;
        }
    }

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

            if (!responseArray.isArray() || responseArray.size() == 0) {
                log.debug("No coach found for team ID: {}", teamId);
                return null;
            }

            JsonNode activeCoach = findActiveCoach(responseArray, teamId);
            
            if (activeCoach != null) {
                log.debug("Found active coach for team ID: {}", teamId);
                return mapToCoachDTO(activeCoach);
            }

            JsonNode mostRecentCoach = findMostRecentCoach(responseArray, teamId);
            
            if (mostRecentCoach != null) {
                log.debug("Found most recent coach (fallback) for team ID: {}", teamId);
                return mapToCoachDTO(mostRecentCoach);
            }

            JsonNode lastCoach = responseArray.get(responseArray.size() - 1);
            log.debug("Using last coach in list (fallback) for team ID: {}", teamId);
            return mapToCoachDTO(lastCoach);

        } catch (Exception e) {
            log.error("Error fetching coach for team ID {}: {}", teamId, e.getMessage());
            return null;
        }
    }

    private JsonNode findActiveCoach(JsonNode coachesArray, Long teamId) {
        for (JsonNode coach : coachesArray) {
            JsonNode career = coach.path("career");
            
            if (!career.isArray()) {
                continue;
            }

            for (JsonNode careerEntry : career) {
                JsonNode team = careerEntry.path("team");
                Long careerTeamId = team.path("id").asLong(0);
                
                if (careerTeamId.equals(teamId)) {
                    String endDate = careerEntry.path("end").asText(null);
                    if (endDate == null || endDate.isEmpty() || "null".equalsIgnoreCase(endDate)) {
                        return coach;
                    }
                }
            }
        }
        return null;
    }

    private JsonNode findMostRecentCoach(JsonNode coachesArray, Long teamId) {
        JsonNode mostRecentCoach = null;
        String mostRecentStartDate = null;

        for (JsonNode coach : coachesArray) {
            JsonNode career = coach.path("career");
            
            if (!career.isArray()) {
                continue;
            }

            for (JsonNode careerEntry : career) {
                JsonNode team = careerEntry.path("team");
                Long careerTeamId = team.path("id").asLong(0);
                
                if (careerTeamId.equals(teamId)) {
                    String startDate = careerEntry.path("start").asText(null);
                    
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

    private CoachDTO mapToCoachDTO(JsonNode coachData) {
        return CoachDTO.builder()
                .name(coachData.path("name").asText(""))
                .photoUrl(coachData.path("photo").asText(""))
                .nationality(coachData.path("nationality").asText(""))
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamData {
        private int founded;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Venue {
        private String name;
        private String image;
        private String city;
        private int capacity;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamResponse {
        private TeamData team;
        private Venue venue;
    }

    public TeamResponse getTeamInfo(Long teamId) {
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

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode responseArray = root.path("response");
            
            if (!responseArray.isArray() || responseArray.size() == 0) {
                log.warn("No team data in response for team ID: {}", teamId);
                return null;
            }

            JsonNode teamResponseNode = responseArray.get(0);
            
            TeamResponse teamResponse = new TeamResponse();
            
            JsonNode teamNode = teamResponseNode.path("team");
            TeamData teamData = new TeamData();
            teamData.setFounded(teamNode.path("founded").asInt(0));
            teamResponse.setTeam(teamData);
            
            JsonNode venueNode = teamResponseNode.path("venue");
            Venue venue = new Venue();
            venue.setName(venueNode.path("name").asText(""));
            venue.setImage(venueNode.path("image").asText(""));
            venue.setCity(venueNode.path("city").asText(""));
            venue.setCapacity(venueNode.path("capacity").asInt(0));
            teamResponse.setVenue(venue);

            log.debug("Fetched team info for team ID: {}", teamId);
            return teamResponse;
        } catch (Exception e) {
            log.error("Error fetching team info for team ID {}: {}", teamId, e.getMessage());
            return null;
        }
    }
}
