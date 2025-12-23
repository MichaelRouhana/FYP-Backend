package com.example.FYP.Api.Model.View;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FixtureViewDTO {

    private Long id;
    private JsonNode rawJson;
    private Long bets;
    private MatchPredictionSettingsView matchPredictionSettings;
    private MatchSettingsView matchSettings;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchPredictionSettingsView {
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
    public static class MatchSettingsView {
        private Boolean allowBettingHT;
        private Boolean showMatch;
        private Boolean allowBetting;
    }


}
