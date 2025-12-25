package com.example.FYP.Api.Job;

import com.example.FYP.Api.Entity.Fixture;
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
    private static final Set<String> FINISHED_STATUSES = Set.of("FT", "AET", "PEN");
    
    // Statuses that indicate a match is currently live
    private static final Set<String> LIVE_STATUSES = Set.of("1H", "2H", "HT", "ET", "P", "BT", "LIVE", "INT");

    /**
     * Resolves live fixtures by fetching fresh data from the Football API.
     * This job runs every 30 seconds to:
     * 1. Find all fixtures currently marked as "live" in the database
     * 2. Fetch the latest status from the Football API for each
     * 3. Update the rawJson with fresh data
     * 4. If a match has finished, resolve the associated bets
     */
    @Scheduled(fixedDelay = 30000) // every 30 seconds
    @CacheEvict(value = "publicFixtures", allEntries = true)
    @Transactional
    public void resolveFinishedFixtures() {
        try {
            // Find all fixtures currently marked as live in the database
            List<Fixture> liveFixtures = fixtureRepository.findAllLiveFixtures();
            
            if (liveFixtures.isEmpty()) {
                log.debug("No live fixtures to resolve");
                return;
            }
            
            log.info("Found {} live fixtures to check for updates", liveFixtures.size());
            int updatedCount = 0;
            int resolvedCount = 0;
            
            for (Fixture fixture : liveFixtures) {
                try {
                    // Fetch fresh data from the Football API for this specific fixture
                    String freshJson = footBallService.getFixtureById(fixture.getId());
                    JsonNode root = objectMapper.readTree(freshJson);
                    JsonNode response = root.path("response");
                    
                    if (response.isArray() && response.size() > 0) {
                        JsonNode freshMatchNode = response.get(0);
                        String freshStatus = freshMatchNode.path("fixture").path("status").path("short").asText();
                        
                        // Get the old status for comparison
                        JsonNode oldJson = objectMapper.readTree(fixture.getRawJson());
                        String oldStatus = oldJson.path("fixture").path("status").path("short").asText();
                        
                        // Always update the rawJson with fresh data
                        fixture.setRawJson(freshMatchNode.toString());
                        fixtureRepository.save(fixture);
                        updatedCount++;
                        
                        // Log status changes
                        if (!oldStatus.equals(freshStatus)) {
                            log.info("Fixture {} status changed: {} -> {}", fixture.getId(), oldStatus, freshStatus);
                        }
                        
                        // If the match has just finished, resolve the bets
                        if (FINISHED_STATUSES.contains(freshStatus)) {
                            log.info("Fixture {} has finished ({}), resolving bets...", fixture.getId(), freshStatus);
                            betResolverService.resolveBetsForFixture(fixture.getId());
                            resolvedCount++;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to update fixture {}: {}", fixture.getId(), e.getMessage());
                }
            }
            
            if (updatedCount > 0 || resolvedCount > 0) {
                log.info("Resolve job completed: {} fixtures updated, {} bets resolved", updatedCount, resolvedCount);
            }
        } catch (Exception e) {
            log.error("Fixture resolve job failed", e);
        }
    }
    
    /**
     * Also check "Not Started" fixtures that might have become live.
     * This handles cases where the sync job missed the transition.
     * Runs every 2 minutes.
     */
    @Scheduled(fixedDelay = 120000) // every 2 minutes
    @CacheEvict(value = "publicFixtures", allEntries = true)
    @Transactional
    public void checkNotStartedFixtures() {
        try {
            List<Fixture> notStartedFixtures = fixtureRepository.findAllNotStartedFixtures();
            
            if (notStartedFixtures.isEmpty()) {
                return;
            }
            
            log.debug("Checking {} not-started fixtures for status changes", notStartedFixtures.size());
            int updatedCount = 0;
            
            for (Fixture fixture : notStartedFixtures) {
                try {
                    // Fetch fresh data
                    String freshJson = footBallService.getFixtureById(fixture.getId());
                    JsonNode root = objectMapper.readTree(freshJson);
                    JsonNode response = root.path("response");
                    
                    if (response.isArray() && response.size() > 0) {
                        JsonNode freshMatchNode = response.get(0);
                        String freshStatus = freshMatchNode.path("fixture").path("status").path("short").asText();
                        
                        // If status has changed from NS to something else, update it
                        if (!freshStatus.equals("NS")) {
                            fixture.setRawJson(freshMatchNode.toString());
                            fixtureRepository.save(fixture);
                            updatedCount++;
                            log.info("Fixture {} transitioned from NS to {}", fixture.getId(), freshStatus);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to check not-started fixture {}: {}", fixture.getId(), e.getMessage());
                }
            }
            
            if (updatedCount > 0) {
                log.info("Updated {} fixtures that transitioned from NS status", updatedCount);
            }
        } catch (Exception e) {
            log.error("Check not-started fixtures job failed", e);
        }
    }
}
