package com.example.FYP.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBettingOnFixtureDTO {
    private Long userId;
    private String username;
    private String avatar;
    private Double totalWagered; // Total stake amount for this fixture
}

