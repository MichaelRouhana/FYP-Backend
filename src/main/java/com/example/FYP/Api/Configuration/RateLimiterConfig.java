package com.example.FYP.Api.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


@Configuration
public class RateLimiterConfig {

    @Bean
    public Bucket bucket() {
        Bandwidth limit = Bandwidth.classic(50,
                Refill.intervally(200, Duration.ofSeconds(60)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
