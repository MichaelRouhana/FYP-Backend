package com.example.FYP.Api.Service;

import com.example.FYP.Api.Model.View.Player.PlayerDetailedStatsDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${football.api.key}")
    private String apiKey;


    public PlayerDetailedStatsDTO getPlayerStats(Long playerId, int season) {
        try {
            String jsonResponse = fetchFromApi("players", Map.of("id", playerId.toString(), "season", String.valueOf(season)));
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.path("response");
            
            if (!response.isArray() || response.size() == 0) {
                throw new EntityNotFoundException("Player not found with ID: " + playerId);
            }

            JsonNode playerData = response.get(0);
            JsonNode statistics = playerData.path("statistics");
            
            if (!statistics.isArray() || statistics.size() == 0) {
                throw new EntityNotFoundException("Statistics not found for player ID: " + playerId);
            }

            JsonNode stats = statistics.get(0); // Use first statistics entry
            JsonNode games = stats.path("games");
            JsonNode goals = stats.path("goals");
            JsonNode shots = stats.path("shots");
            JsonNode dribbles = stats.path("dribbles");
            JsonNode passes = stats.path("passes");
            JsonNode tackles = stats.path("tackles");
            JsonNode duels = stats.path("duels");
            JsonNode cards = stats.path("cards");
            JsonNode fouls = stats.path("fouls");
            JsonNode penalty = stats.path("penalty");

            PlayerDetailedStatsDTO.Summary summary = PlayerDetailedStatsDTO.Summary.builder()
                    .matchesPlayed(safeIntOrZero(games.path("appearences")))
                    .minutesPlayed(safeIntOrZero(games.path("minutes")))
                    .goals(safeIntOrZero(goals.path("total")))
                    .assists(safeIntOrZero(goals.path("assists")))
                    .rating(safeStringOrDash(games.path("rating")))
                    .build();

            PlayerDetailedStatsDTO.Attacking attacking = PlayerDetailedStatsDTO.Attacking.builder()
                    .shotsTotal(safeIntOrZero(shots.path("total")))
                    .shotsOnTarget(safeIntOrZero(shots.path("on")))
                    .dribblesAttempted(safeIntOrZero(dribbles.path("attempts")))
                    .dribblesSuccess(safeIntOrZero(dribbles.path("success")))
                    .penaltiesScored(safeIntOrZero(penalty.path("scored")))
                    .penaltiesMissed(safeIntOrZero(penalty.path("missed")))
                    .build();

            Integer passAccuracy = 0;
            if (!passes.path("accuracy").isMissingNode() && !passes.path("accuracy").isNull()) {
                String accuracyStr = passes.path("accuracy").asText("");
                if (!accuracyStr.isEmpty()) {
                    // Remove % sign if present and parse
                    String cleaned = accuracyStr.replace("%", "").trim();
                    try {
                        passAccuracy = Integer.parseInt(cleaned);
                    } catch (NumberFormatException e) {
                        log.debug("Could not parse pass accuracy: {}", accuracyStr);
                    }
                }
            }

            PlayerDetailedStatsDTO.Passing passing = PlayerDetailedStatsDTO.Passing.builder()
                    .totalPasses(safeIntOrZero(passes.path("total")))
                    .keyPasses(safeIntOrZero(passes.path("key")))
                    .passAccuracy(passAccuracy)
                    .build();

            PlayerDetailedStatsDTO.Defending defending = PlayerDetailedStatsDTO.Defending.builder()
                    .tacklesTotal(safeIntOrZero(tackles.path("total")))
                    .interceptions(safeIntOrZero(tackles.path("interceptions")))
                    .blocks(safeIntOrZero(tackles.path("blocks")))
                    .duelsTotal(safeIntOrZero(duels.path("total")))
                    .duelsWon(safeIntOrZero(duels.path("won")))
                    .build();

            PlayerDetailedStatsDTO.Discipline discipline = PlayerDetailedStatsDTO.Discipline.builder()
                    .yellowCards(safeIntOrZero(cards.path("yellow")))
                    .redCards(safeIntOrZero(cards.path("red")))
                    .foulsCommitted(safeIntOrZero(fouls.path("committed")))
                    .foulsDrawn(safeIntOrZero(fouls.path("drawn")))
                    .build();

            return PlayerDetailedStatsDTO.builder()
                    .summary(summary)
                    .attacking(attacking)
                    .passing(passing)
                    .defending(defending)
                    .discipline(discipline)
                    .build();

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching player stats for ID {}: {}", playerId, e.getMessage());
            throw new EntityNotFoundException("Failed to fetch player statistics: " + e.getMessage());
        }
    }

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

    private Integer safeIntOrZero(JsonNode node) {
        Integer value = safeInt(node);
        return value != null ? value : 0;
    }

    private String safeString(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        String text = node.asText("");
        return text.isEmpty() ? null : text;
    }

    private String safeStringOrDash(JsonNode node) {
        String value = safeString(node);
        return value != null && !value.isEmpty() ? value : "-";
    }
}

