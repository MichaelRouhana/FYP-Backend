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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureSyncService {

    private final FootBallService footBallService;
    private final FixtureRepository fixtureRepository;
    private final ObjectMapper objectMapper;
    
    // Statuses that indicate a match is finished
    private static final Set<String> FINISHED_STATUSES = Set.of("FT", "AET", "PEN", "PST", "CANC", "ABD", "AWD", "WO");

    /**
     * Syncs fixtures for a given date from the Football API.
     * - If fixture exists: compares old vs new status, updates rawJson
     * - If status changed to finished: sets allowBetting = false
     * - If new fixture: creates with default settings
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
            int finishedCount = 0;

            for (JsonNode matchNode : response) {
                Long fixtureId = matchNode.path("fixture").path("id").asLong();
                String newStatus = matchNode.path("fixture").path("status").path("short").asText();
                
                if (fixtureId == 0) {
                    log.warn("Skipping fixture with invalid ID");
                    continue;
                }

                // Check if fixture already exists
                Optional<Fixture> existingFixtureOpt = fixtureRepository.findById(fixtureId);
                
                if (existingFixtureOpt.isPresent()) {
                    Fixture fixture = existingFixtureOpt.get();
                    
                    // Get old status for comparison
                    String oldStatus = "";
                    try {
                        JsonNode oldJson = objectMapper.readTree(fixture.getRawJson());
                        oldStatus = oldJson.path("fixture").path("status").path("short").asText();
                    } catch (Exception e) {
                        log.warn("Could not parse old status for fixture {}", fixtureId);
                    }
                    
                    // Update the rawJson
                    fixture.setRawJson(matchNode.toString());
                    
                    // Ensure MatchSettings exist (auto-create if missing)
                    if (fixture.getMatchSettings() == null) {
                        fixture.setMatchSettings(MatchSettings.builder()
                                .allowBetting(true)
                                .allowBettingHT(true)
                                .showMatch(true)
                                .build());
                    }
                    
                    // If status changed to finished, disable betting
                    if (!oldStatus.equals(newStatus) && FINISHED_STATUSES.contains(newStatus)) {
                        fixture.getMatchSettings().setAllowBetting(false);
                        fixture.getMatchSettings().setAllowBettingHT(false);
                        // Keep showMatch as true (admin can still see finished matches)
                        finishedCount++;
                        log.info("Fixture {} finished ({} -> {}), betting disabled", 
                                fixtureId, oldStatus, newStatus);
                    }
                    
                    fixtureRepository.save(fixture);
                    updatedCount++;
                } else {
                    // Create new fixture with default settings
                    // If already finished, disable betting from the start
                    boolean isFinished = FINISHED_STATUSES.contains(newStatus);
                    
                    Fixture fixture = Fixture.builder()
                            .id(fixtureId)
                            .rawJson(matchNode.toString())
                            .matchSettings(MatchSettings.builder()
                                    .allowBetting(!isFinished) // Disable if already finished
                                    .allowBettingHT(true) // Default to true (can be changed by admin)
                                    .showMatch(true) // Default to true (can be changed by admin)
                                    .build())
                            .matchPredictionSettings(defaultPredictionSettings())
                            .build();
                    fixtureRepository.save(fixture);
                    newCount++;
                }
            }

            log.info("Fixture sync for {}: {} new, {} updated, {} finished", 
                    date, newCount, updatedCount, finishedCount);

        } catch (Exception e) {
            log.error("Failed to sync fixtures for date {}", date, e);
            throw new RuntimeException(e);
        }
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
