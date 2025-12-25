package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchPredictionSettings;
import com.example.FYP.Api.Entity.MatchSettings;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureSyncService {

    private final FootBallService footBallService;
    private final FixtureRepository fixtureRepository;
    private final ObjectMapper objectMapper;

    /**
     * Syncs fixtures for a given date from the Football API.
     * If a fixture already exists, only updates the rawJson (preserves settings).
     * If it's new, creates it with default settings.
     */
    @Transactional
    @CacheEvict(value = "publicFixtures", allEntries = true)
    public void syncFixtures(String date) {
        String json = footBallService.getFixturesByDate(date);

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode response = root.get("response");
            
            if (response == null || !response.isArray()) {
                log.warn("No response array in API data for date {}", date);
                return;
            }

            int newCount = 0;
            int updatedCount = 0;

            for (JsonNode matchNode : response) {
                Long fixtureId = matchNode.path("fixture").path("id").asLong();
                
                if (fixtureId == 0) {
                    log.warn("Skipping fixture with invalid ID");
                    continue;
                }

                // Check if fixture already exists
                Optional<Fixture> existingFixture = fixtureRepository.findById(fixtureId);
                
                if (existingFixture.isPresent()) {
                    // Update only the rawJson, preserve existing settings
                    Fixture fixture = existingFixture.get();
                    fixture.setRawJson(matchNode.toString());
                    fixtureRepository.save(fixture);
                    updatedCount++;
                } else {
                    // Create new fixture with default settings
                    Fixture fixture = Fixture.builder()
                            .id(fixtureId)
                            .rawJson(matchNode.toString())
                            .matchSettings(defaultMatchSettings())
                            .matchPredictionSettings(defaultPredictionSettings())
                            .build();
                    fixtureRepository.save(fixture);
                    newCount++;
                }
            }

            log.info("Fixture sync for {}: {} new, {} updated", date, newCount, updatedCount);

        } catch (Exception e) {
            log.error("Failed to sync fixtures for date {}", date, e);
            throw new RuntimeException(e);
        }
    }

    private MatchSettings defaultMatchSettings() {
        return MatchSettings.builder()
                .allowBetting(true)
                .allowBettingHT(false)
                .showMatch(true)
                .build();
    }

    private MatchPredictionSettings defaultPredictionSettings() {
        return MatchPredictionSettings.builder()
                .whoWillWin(true)
                .doubleChance(true)
                .goalsOverUnder(true)
                .bothTeamsScore(true)
                .firstTeamToScore(true)
                .scorePrediction(false)
                .build();
    }
}
