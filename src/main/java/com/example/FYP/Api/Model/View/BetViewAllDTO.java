package com.example.FYP.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
