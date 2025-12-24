package com.example.FYP.Api.Job;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.example.FYP.Api.Service.BetResolverService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixtureResolveJob {

    private final FixtureRepository fixtureRepository;
    private final BetResolverService betResolverService;
    private final ObjectMapper objectMapper;

    /**
     * Resolves finished fixtures and their bets.
     * Runs every 30 seconds to check for matches that have finished.
     */
    @Scheduled(fixedDelay = 30000) // every 30 seconds
    @CacheEvict(value = "publicFixtures", allEntries = true)
    public void resolveFinishedFixtures() {
        try {
            List<Fixture> allFixtures = fixtureRepository.findAll();
            int resolvedCount = 0;
            
            for (Fixture fixture : allFixtures) {
                try {
                    JsonNode rawJson = objectMapper.readTree(fixture.getRawJson());
                    String statusShort = rawJson.path("fixture").path("status").path("short").asText();
                    
                    // Check if match is finished (FT, AET, PEN)
                    if ("FT".equals(statusShort) || "AET".equals(statusShort) || "PEN".equals(statusShort)) {
                        // Resolve bets for this fixture
                        betResolverService.resolveBetsForFixture(fixture.getId());
                        resolvedCount++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to check fixture {}: {}", fixture.getId(), e.getMessage());
                }
            }
            
            if (resolvedCount > 0) {
                log.info("Resolved {} finished fixtures", resolvedCount);
            }
        } catch (Exception e) {
            log.error("Fixture resolve job failed", e);
        }
    }
}
