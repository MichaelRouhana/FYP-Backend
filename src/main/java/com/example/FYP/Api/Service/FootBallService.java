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
     * Fetches coach details for a specific team from the Football API.
     * 
     * @param teamId The team ID
     * @return CoachDTO with coach information, or null if no coach found or API call fails
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

            // Get the first coach (teams typically have one active coach)
            JsonNode coachData = responseArray.get(0);

            return CoachDTO.builder()
                    .name(coachData.path("name").asText(""))
                    .photoUrl(coachData.path("photo").asText(""))
                    .nationality(coachData.path("nationality").asText(""))
                    .build();

        } catch (Exception e) {
            log.error("Error fetching coach for team ID {}: {}", teamId, e.getMessage());
            return null; // Return null on error, allowing fallback handling
        }
    }
}
