package com.example.FYP.Api.Model.View.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamHeaderDTO {
    private String name;
    private String logo;
    private Integer foundedYear;
    private String country;
    private String stadiumName;
    private String coachName;
    private Integer uefaRanking;
}

