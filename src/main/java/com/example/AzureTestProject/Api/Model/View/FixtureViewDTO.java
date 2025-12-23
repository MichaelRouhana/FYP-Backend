package com.example.AzureTestProject.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FixtureViewDTO {

    private Long id;
    private String rawJson;
    private MatchPredictionSettingsView matchPredictionSettings;
    private MatchSettingsView matchSettings;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class MatchPredictionSettingsView {
        private Boolean goalsOverUnder;
        private Boolean bothTeamsScore;
        private Boolean firstTeamToScore;
        private Boolean doubleChance;
        private Boolean scorePrediction;
        private Boolean whoWillWin;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class MatchSettingsView {
        private Boolean allowBettingHT;
        private Boolean showMatch;
        private Boolean allowBetting;
    }


}
