package com.example.FYP.Api.Model.View.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetailsDTO {
    private String stadiumName;
    private String stadiumImage;
    private String city;
    private int capacity;
    private int foundedYear;
}

