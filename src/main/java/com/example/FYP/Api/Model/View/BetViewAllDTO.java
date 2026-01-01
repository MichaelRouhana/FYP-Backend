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
}
