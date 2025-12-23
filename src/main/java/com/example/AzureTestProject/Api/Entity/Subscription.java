package com.example.AzureTestProject.Api.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "subscription")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Organization organization;

    @ManyToOne
    private Plan plan;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    public enum SubscriptionStatus {
        ACTIVE,
        PENDING,
        EXPIRED,
        CANCELLED,
        SUSPENDED
    }

}
