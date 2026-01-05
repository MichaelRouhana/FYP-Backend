package com.example.FYP.Api.Model.View;

import lombok.Data;

import java.util.List;

@Data
public class UserViewDTO {
    private String username;
    private String email;
    private String pfp;
    private List<String> roles;
    // Profile fields
    private Long totalPoints; // User's balance/points
    private Long totalBets; // Total number of bets
    private Long totalWins; // Total number of won bets
    private Double winRate; // Win rate percentage (0.0 to 100.0)
}
