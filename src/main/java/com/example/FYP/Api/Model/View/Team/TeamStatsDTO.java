package com.example.FYP.Api.Model.View.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatsDTO {
    // Summary
    private Integer matchesPlayed;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalDifference;
    private Integer cleanSheets;
    
    // Attacking
    private Integer totalGoalsFor;
    private String goalsForAverage;
    private Integer shotsTotal;
    private Integer shotsOnTarget;
    private Integer penaltiesScored;
    
    // Passing
    private Integer passesTotal;
    private Integer passesAccurate;
    private String passAccuracyPercentage;
    
    // Defending
    private Integer totalGoalsAgainst;
    private String goalsAgainstAverage;
    private Integer tacklesTotal;
    private Integer interceptionsTotal;
    private Integer savesTotal;
    
    // Other
    private Integer yellowCards;
    private Integer redCards;
    private Integer foulsCommitted;
}

