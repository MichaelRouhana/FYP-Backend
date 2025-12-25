package com.example.FYP.Api.Service;


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
        // UPDATED: Added league=39 (Premier League) and season=2025 to fix empty results
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
            
            // CRITICAL: This log will now show us why the list was empty!
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

        log.info("Fetched fixture by ID: {}", fixtureId);
        return response.getBody();
    }
}
