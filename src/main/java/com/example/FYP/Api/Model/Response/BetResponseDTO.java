package com.example.FYP.Api.Model.Response;

import com.example.FYP.Api.Entity.BetStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetResponseDTO {
    private Long id; // ID of the first bet (or accumulator bet ID if we add that entity)
    private Double stake;
    private BigDecimal totalOdds; // Combined odds of all legs
    private BigDecimal potentialWinnings; // stake * totalOdds
    private BetStatus status;
    private List<BetLegResponseDTO> legs; // Array of bet legs
    
    // Match details from the first leg's fixture (for main display)
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
