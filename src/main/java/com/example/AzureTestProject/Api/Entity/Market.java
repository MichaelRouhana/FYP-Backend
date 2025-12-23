package com.example.AzureTestProject.Api.Entity;

import jakarta.persistence.*;

@Entity
public class Market {

    @Id
    @GeneratedValue
    private Long id;

    private Long fixtureId;

    @Enumerated(EnumType.STRING)
    private MarketType marketType;

    private boolean active;
}
