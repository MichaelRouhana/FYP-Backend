package com.example.FYP.Api.Model.Response;

import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.MarketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetLegResponseDTO {
    private Long id;
    private Long fixtureId;
    private MarketType marketType;
    private String selection;
    private BigDecimal odd;
    private BetStatus status;
}

