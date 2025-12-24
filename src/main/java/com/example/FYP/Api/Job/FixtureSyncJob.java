package com.example.FYP.Api.Job;

import com.example.FYP.Api.Service.FixtureSyncService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixtureSyncJob {

    private final FixtureSyncService syncService;

    /**
     * Sync fixtures on application startup
     */
    @PostConstruct
    public void syncOnStartup() {
        log.info("Syncing fixtures on startup...");
        try {
            // Sync today and next 7 days on startup
            for (int i = 0; i <= 7; i++) {
                String date = LocalDate.now().plusDays(i).toString();
                syncService.syncFixtures(date);
                log.info("Synced fixtures for {}", date);
            }
            log.info("Startup fixture sync completed");
        } catch (Exception e) {
            log.error("Startup fixture sync failed", e);
        }
    }

    /**
     * Sync today and tomorrow every 5 minutes to keep data fresh
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes
    public void syncTodayAndTomorrow() {
        try {
            syncService.syncFixtures(LocalDate.now().toString());
            syncService.syncFixtures(LocalDate.now().plusDays(1).toString());
            log.info("Scheduled sync completed for today and tomorrow");
        } catch (Exception e) {
            log.error("Fixture sync failed", e);
        }
    }
}
