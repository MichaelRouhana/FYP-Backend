package com.example.FYP.Api.Model.View;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetViewAllDTO {
    private Long id;
    private Long fixtureId;
    private String marketType;
    private String selection;
    private Double stake;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate; // For grouping accumulator bets
    
    // Fixture details
    private String homeTeam;
    private String awayTeam;
    private String homeTeamLogo;
    private String awayTeamLogo;
    private Integer homeScore;
    private Integer awayScore;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime matchDate;
    private String matchStatus; // e.g., "FT", "NS", "LIVE"
}
