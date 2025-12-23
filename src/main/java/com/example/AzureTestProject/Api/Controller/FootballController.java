package com.example.AzureTestProject.Api.Controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/football")
public class FootballController {


    private String apiKey = "77cd6e963dd1f4c1d704edbe96289cf3";

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/{endpoint}")
    public ResponseEntity<String> forwardRequest(
            @PathVariable String endpoint,
            @RequestParam Map<String, String> params
    ) {
        List<String> allowed = List.of("fixtures", "teams", "leagues", "standings");
        if (!allowed.contains(endpoint)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"Endpoint not allowed\"}");
        }

        String baseUrl = "https://v3.football.api-sports.io/" + endpoint;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        params.forEach(builder::queryParam);
        String targetUrl = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, HttpMethod.GET, entity, String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

}
