package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchPredictionSettings;
import com.example.FYP.Api.Entity.MatchSettings;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureSyncService {

    private final FootBallService footBallService;
    private final FixtureRepository fixtureRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncFixtures(String date) {

        String json = footBallService.getFixturesByDate(date);

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode response = root.get("response");

            for (JsonNode matchNode : response) {

                Long fixtureId = matchNode
                        .get("fixture")
                        .get("id")
                        .asLong();

                Fixture fixture = Fixture.builder()
                        .id(fixtureId) // 🔥 PRIMARY KEY
                        .rawJson(matchNode.toString())
                        .matchSettings(defaultMatchSettings())
                        .matchPredictionSettings(defaultPredictionSettings())
                        .build();

                fixtureRepository.save(fixture);
            }

            log.info("Fixture sync completed for {}", date);

        } catch (Exception e) {
            log.error("Failed to sync fixtures", e);
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
