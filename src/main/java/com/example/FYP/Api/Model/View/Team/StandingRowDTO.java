package com.example.FYP.Api.Model.View.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandingRowDTO {
    private Integer rank;
    private String team;
    private String teamLogo;
    private Integer mp; // Matches Played
    private Integer w;  // Wins
    private Integer d;  // Draws
    private Integer l;  // Losses
    private Integer gd; // Goal Difference
    private Integer pts; // Points
    private Boolean isCurrent;
}

