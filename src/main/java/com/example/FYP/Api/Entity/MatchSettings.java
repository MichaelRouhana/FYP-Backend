package com.example.FYP.Api.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowBettingHT = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean showMatch = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowBetting = false;
}
