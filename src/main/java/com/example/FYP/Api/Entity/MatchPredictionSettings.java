package com.example.FYP.Api.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_prediction_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchPredictionSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private Boolean goalsOverUnder = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean bothTeamsScore = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean firstTeamToScore = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean doubleChance = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean scorePrediction = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean whoWillWin = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean halfTimeFullTime = true;
}
