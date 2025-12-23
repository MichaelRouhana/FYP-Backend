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
    private Boolean goalsOverUnder;

    @Column(nullable = false)
    private Boolean bothTeamsScore;

    @Column(nullable = false)
    private Boolean firstTeamToScore;

    @Column(nullable = false)
    private Boolean doubleChance;

    @Column(nullable = false)
    private Boolean scorePrediction;

    @Column(nullable = false)
    private Boolean whoWillWin;
}
