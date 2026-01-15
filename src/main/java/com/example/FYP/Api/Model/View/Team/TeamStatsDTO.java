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
    private Integer matchesPlayed;
    private Integer goalsScored;
    private Double goalsPerGame;
    private Integer cleanSheets;
    private Integer yellowCards;
    private Integer redCards;
}

