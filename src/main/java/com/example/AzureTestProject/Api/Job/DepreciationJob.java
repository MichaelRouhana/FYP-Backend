package com.example.AzureTestProject.Api.Job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DepreciationJob {


    @Scheduled(cron = "0 0 15 * * ?")
    public void runDailyJob() {

    }

}
