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
    private Boolean allowBettingHT;

    @Column(nullable = false)
    private Boolean showMatch;

    @Column(nullable = false)
    private Boolean allowBetting;
}
