package com.example.FYP.Api.Model.Response;

import com.example.FYP.Api.Entity.BetStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetResponseDTO {
    private Long id;
    private Double stake;
    private BigDecimal totalOdds;
    private BigDecimal potentialWinnings;
    private BetStatus status;
    private List<BetLegResponseDTO> legs;

    // --- Added fields to fix Frontend Crash & Missing History ---
    private Long fixtureId;
    private String homeTeam;
    private String awayTeam;
    private String homeTeamLogo;
    private String awayTeamLogo;
    private Integer homeScore;
    private Integer awayScore;
    private String matchDate;   // Sent as String to frontend for simplicity
    private String matchStatus; // e.g. "FT", "NS"
}