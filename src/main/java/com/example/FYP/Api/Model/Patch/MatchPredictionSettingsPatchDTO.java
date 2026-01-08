package com.example.FYP.Api.Model.Patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPredictionSettingsPatchDTO {
    private Boolean goalsOverUnder;
    private Boolean bothTeamsScore;
    private Boolean firstTeamToScore;
    private Boolean doubleChance;
    private Boolean scorePrediction;
    private Boolean whoWillWin;
    private Boolean halfTimeFullTime;
}
