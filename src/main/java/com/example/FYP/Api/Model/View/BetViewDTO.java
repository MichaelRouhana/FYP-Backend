package com.example.FYP.Api.Model.View;

import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.MarketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetViewDTO {
    private Long id;
    private Long fixtureId;
    private MarketType marketType;
    private Double stake;
    private String selection;
    private BetStatus betStatus;
}
