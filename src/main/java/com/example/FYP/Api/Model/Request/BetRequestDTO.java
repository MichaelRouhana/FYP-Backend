package com.example.FYP.Api.Model.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetRequestDTO {
    private Double stake; // Total stake for the accumulator bet
    private List<BetLegDTO> legs; // Array of predictions/legs
}
