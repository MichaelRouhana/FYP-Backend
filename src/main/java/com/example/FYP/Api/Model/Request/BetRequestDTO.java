package com.example.FYP.Api.Model.Request;

import com.example.FYP.Api.Entity.MarketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetRequestDTO {
    private Long fixtureId;
    private MarketType marketType;
    private String selection;
}
