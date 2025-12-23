package com.example.FYP.Api.Entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "fixtures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fixture extends AuditableEntity{

    @Id
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawJson;


    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "fixture", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Bet> betsSet = new HashSet<>();


    public Long getBets() {
        return (long) betsSet.size();
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "match_prediction_settings_id")
    private MatchPredictionSettings matchPredictionSettings;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "match_settings_id")
    private MatchSettings matchSettings;

    @Transient
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---- Helper methods for BetResolverService ----

    /** Returns goals as a map with "home" and "away" keys */
    public Map<String, Integer> getGoals() {
        try {
            JsonNode node = objectMapper.readTree(rawJson).path("goals");
            if (node.isMissingNode() || node.isNull()) return null;
            return Map.of(
                    "home", node.path("home").asInt(0),
                    "away", node.path("away").asInt(0)
            );
        } catch (IOException e) {
            return null;
        }
    }

    /** Returns first team to score: "HOME" or "AWAY" or null */
    public String getFirstTeamToScore() {
        try {
            JsonNode node = objectMapper.readTree(rawJson).path("score").path("halftime");
            if (node.isMissingNode() || node.isNull()) return null;
            int home = node.path("home").asInt(0);
            int away = node.path("away").asInt(0);
            if (home > 0) return "HOME";
            if (away > 0) return "AWAY";
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /** Returns home team name */
    public String getHomeTeamName() {
        return getString("teams.home.name");
    }

    /** Returns away team name */
    public String getAwayTeamName() {
        return getString("teams.away.name");
    }

    /** Returns fixture status short (e.g., "FT") */
    public String getStatusShort() {
        return getString("fixture.status.short");
    }

    /** Returns fixture status long (e.g., "Match Finished") */
    public String getStatusLong() {
        return getString("fixture.status.long");
    }

    /** Returns fixture external ID from API */
    public Long getExternalFixtureId() {
        return getLong("fixture.id");
    }

    /** Generic helper methods to parse JSON paths safely */
    private String getString(String path) {
        try {
            JsonNode node = getNode(path);
            return node.isMissingNode() || node.isNull() ? null : node.asText();
        } catch (IOException e) {
            return null;
        }
    }

    private Long getLong(String path) {
        try {
            JsonNode node = getNode(path);
            return node.isMissingNode() || node.isNull() ? null : node.asLong();
        } catch (IOException e) {
            return null;
        }
    }

    private JsonNode getNode(String path) throws IOException {
        JsonNode node = objectMapper.readTree(rawJson);
        String[] parts = path.split("\\.");
        for (String part : parts) {
            node = node.path(part);
        }
        return node;
    }
}
