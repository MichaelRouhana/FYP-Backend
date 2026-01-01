package com.example.FYP.Api.Model.Response;

import com.example.FYP.Api.Entity.BetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
}
