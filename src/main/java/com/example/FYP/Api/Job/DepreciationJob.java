package com.example.FYP.Api.Job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepreciationJob {


    @Scheduled(cron = "0 0 15 * * ?")
    public void runDailyJob() {

    }

}
