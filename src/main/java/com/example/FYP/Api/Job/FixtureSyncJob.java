package com.example.FYP.Api.Job;

import com.example.FYP.Api.Service.FixtureSyncService;
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

    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes
    public void syncTodayAndTomorrow() {
        try {
            syncService.syncFixtures(LocalDate.now().toString());
            syncService.syncFixtures(LocalDate.now().plusDays(1).toString());
        } catch (Exception e) {
            log.error("Fixture sync failed", e);
        }
    }


}
