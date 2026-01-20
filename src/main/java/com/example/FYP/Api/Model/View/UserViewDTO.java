package com.example.FYP.Api.Model.View;

import lombok.Data;

import java.util.List;

@Data
public class UserViewDTO {
    private Long id; // User ID
    private String username;
    private String email;
    private String pfp;
    private List<String> roles; // Community roles: 'OWNER', 'MODERATOR', 'MEMBER'
    // Profile fields
    private Long totalPoints; // User's balance/points
    private Long totalBets; // Total number of bets (including pending)
    private Long totalWins; // Total number of won bets
    private Long totalLost; // Total number of lost bets (excluding pending)
    private Double winRate; // Win rate percentage (0.0 to 100.0)
    private String about; // User's bio/about section
    private String country; // User's country from address (if available)
}
