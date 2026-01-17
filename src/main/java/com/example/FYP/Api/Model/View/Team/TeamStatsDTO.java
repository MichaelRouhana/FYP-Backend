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
    private Integer goalsScored;
    private String goalsPerMatch;
    private Integer shots;
    private Integer shotsOnTarget;
    private Integer penaltiesScored;
    
    // Passing
    private Integer passes;
    private Integer passesAccurate;
    private String passAccuracy;
    
    // Defending
    private Integer goalsConceded;
    private String goalsConcededPerMatch;
    private Integer tackles;
    private Integer interceptions;
    private Integer saves;
    
    // Other
    private Integer yellowCards;
    private Integer redCards;
    private Integer fouls;
}

