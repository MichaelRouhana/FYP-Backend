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

    private String apiKey = "77cd6e963dd1f4c1d704edbe96289cf3";

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

        String url = "https://v3.football.api-sports.io/fixtures?date=" + date;

        HttpHeaders headers = new HttpHeaders();
        headers.put("x-apisports-key", Collections.singletonList(apiKey));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        log.info("Fixtures fetched for date {}", date);
        return response.getBody();
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
