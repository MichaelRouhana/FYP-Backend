package com.example.AzureTestProject.Api.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bet extends AuditableEntity {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Enumerated(EnumType.STRING)
    private MarketType marketType;

    private String selection;

    private Double stake;

    private BigDecimal odd; // SNAPSHOT

    @Enumerated(EnumType.STRING)
    private BetStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
