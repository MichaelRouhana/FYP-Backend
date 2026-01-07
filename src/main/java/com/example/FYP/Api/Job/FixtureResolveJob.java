package com.example.FYP.Api.Job;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchSettings;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.example.FYP.Api.Service.BetResolverService;
import com.example.FYP.Api.Service.FootBallService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixtureResolveJob {

    private final FixtureRepository fixtureRepository;
    private final BetResolverService betResolverService;
    private final FootBallService footBallService;
    private final ObjectMapper objectMapper;
    
    // Statuses that indicate a match is finished
    private static final Set<String> FINISHED_STATUSES = Set.of("FT", "AET", "PEN", "PST", "CANC", "ABD", "AWD", "WO");

    /**
     * MAIN RESOLVE JOB: Finds ALL matches where status is NOT finished 
     * but the scheduled start time has already passed.
     * Fetches fresh status by Match ID (bypasses stale bulk sync).
     * Also checks for finished fixtures with pending bets (retry logic).
     * Runs every 15 seconds.
     */
    @Scheduled(fixedDelay = 15000) // every 15 seconds
    @CacheEvict(value = "publicFixtures", allEntries = true)
    @Transactional
    public void resolveFinishedFixtures() {
        try {
            // First, check for finished fixtures with pending bets (retry logic)
            // This ensures we retry resolution for any missed bets from previous runs
            List<Fixture> finishedWithPendingBets = fixtureRepository.findFinishedFixturesWithPendingBets();
            if (!finishedWithPendingBets.isEmpty()) {
                log.info("Found {} finished fixtures with pending bets, retrying resolution", finishedWithPendingBets.size());
            }
            for (Fixture fixture : finishedWithPendingBets) {
                try {
                    log.info("Retrying bet resolution for finished fixture {} with pending bets", fixture.getId());
                    betResolverService.resolveBetsForFixture(fixture);
                } catch (Exception e) {
                    log.error("Failed to resolve bets for finished fixture {}: {}", fixture.getId(), e.getMessage(), e);
                }
            }

            // Find ALL fixtures that are not yet marked as finished
            List<Fixture> unfinishedFixtures = fixtureRepository.findAllUnfinishedFixtures();
            
            if (unfinishedFixtures.isEmpty()) {
                log.debug("No unfinished fixtures to check");
                return;
            }
            
            Instant now = Instant.now();
            int checkedCount = 0;
            int updatedCount = 0;
            int finishedCount = 0;
            
            for (Fixture fixture : unfinishedFixtures) {
                try {
                    // Parse the scheduled start time from rawJson
                    JsonNode currentJson = objectMapper.readTree(fixture.getRawJson());
                    String dateStr = currentJson.path("fixture").path("date").asText();
                    String currentStatus = currentJson.path("fixture").path("status").path("short").asText();
                    
                    if (dateStr == null || dateStr.isEmpty()) {
                        continue;
                    }
                    
                    // Only check matches whose start time has passed (with 30 min buffer for delays)
                    Instant scheduledStart = Instant.parse(dateStr);
                    if (now.isBefore(scheduledStart.minus(30, ChronoUnit.MINUTES))) {
                        // Match hasn't started yet, skip
                        continue;
                    }
                    
                    checkedCount++;
                    
                    // Fetch fresh data by Match ID (real-time truth)
                    String freshJson = footBallService.getFixtureById(fixture.getId());
                    JsonNode root = objectMapper.readTree(freshJson);
                    JsonNode response = root.path("response");
                    
                    if (response.isArray() && response.size() > 0) {
                        JsonNode freshMatchNode = response.get(0);
                        String freshStatus = freshMatchNode.path("fixture").path("status").path("short").asText();
                        
                        // Only update if status has changed
                        if (!currentStatus.equals(freshStatus)) {
                            // Update the rawJson with fresh data
                            fixture.setRawJson(freshMatchNode.toString());
                            
                            // If match is now finished, disable betting
                            if (FINISHED_STATUSES.contains(freshStatus)) {
                                if (fixture.getMatchSettings() == null) {
                                    fixture.setMatchSettings(MatchSettings.builder()
                                            .allowBetting(false)
                                            .allowBettingHT(false)
                                            .showMatch(true)
                                            .build());
                                } else {
                                    fixture.getMatchSettings().setAllowBetting(false);
                                    fixture.getMatchSettings().setAllowBettingHT(false);
                                }
                                
                                // Resolve bets for this fixture
                                betResolverService.resolveBetsForFixture(fixture.getId());
                                finishedCount++;
                                log.info("Fixture {} finished ({} -> {}), betting disabled, bets resolved", 
                                        fixture.getId(), currentStatus, freshStatus);
                            } else {
                                log.info("Fixture {} status changed: {} -> {}", 
                                        fixture.getId(), currentStatus, freshStatus);
                            }
                            
                            fixtureRepository.save(fixture);
                            updatedCount++;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to check fixture {}: {}", fixture.getId(), e.getMessage());
                }
            }
            
            if (checkedCount > 0) {
                log.debug("Resolve job: checked {}, updated {}, finished {}", 
                        checkedCount, updatedCount, finishedCount);
            }
        } catch (Exception e) {
            log.error("Fixture resolve job failed", e);
        }
    }
}
