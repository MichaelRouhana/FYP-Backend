package com.example.FYP.Api.Model.View;

import lombok.Data;

import java.util.List;

@Data
public class UserViewDTO {
    private Long id; // User ID
    private String username;
    private String email;
    private String pfp;
    private List<String> roles;
    private Long totalPoints;
    private Long totalBets;
    private Long totalWins;
    private Long totalLost;
    private Double winRate;
    private String about;
    private String country;
}
