package com.example.FYP.Api.Model.Request;

import com.example.FYP.Api.Entity.MarketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetLegDTO {
    private Long fixtureId;
    private MarketType marketType;
    private String selection;
    private BigDecimal odd;
}

