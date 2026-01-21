package com.example.FYP.Api.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/football")
public class FootballController {


    @Value("${football.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final List<String> ALLOWED_ENDPOINTS = List.of(
            "fixtures",
            "fixtures/lineups",
            "fixtures/statistics",
            "fixtures/events",
            "fixtures/headtohead",
            "fixtures/predictions",
            "fixtures/players",
            "teams",
            "teams/statistics",
            "leagues",
            "standings",
            "players",
            "players/seasons",
            "players/statistics",
            "injuries",
            "odds",
            "odds/bets"
    );

    @GetMapping("/{endpoint}")
    public ResponseEntity<String> forwardRequest(
            @PathVariable String endpoint,
            @RequestParam Map<String, String> params
    ) {
        return forwardToFootballApi(endpoint, params);
    }

    @GetMapping("/{endpoint}/{subEndpoint}")
    public ResponseEntity<String> forwardSubRequest(
            @PathVariable String endpoint,
            @PathVariable String subEndpoint,
            @RequestParam Map<String, String> params
    ) {
        String fullEndpoint = endpoint + "/" + subEndpoint;
        return forwardToFootballApi(fullEndpoint, params);
    }

    private ResponseEntity<String> forwardToFootballApi(String endpoint, Map<String, String> params) {
        if (!ALLOWED_ENDPOINTS.contains(endpoint)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"Endpoint not allowed: " + endpoint + "\"}");
        }

        String baseUrl = "https://v3.football.api-sports.io/" + endpoint;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        params.forEach(builder::queryParam);
        String targetUrl = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl, HttpMethod.GET, entity, String.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to fetch from Football API: " + e.getMessage() + "\"}");
        }
    }

}
