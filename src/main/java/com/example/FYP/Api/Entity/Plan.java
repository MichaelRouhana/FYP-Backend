package com.example.FYP.Api.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Entity(name = "plan")
@Data
public class Plan {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private BigDecimal price;
    private Integer durationInDays;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Feature> features;

    public enum SubscriptionPlan {
        FREE, ENTERPRISE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plan plan)) return false;
        return subscriptionPlan == plan.subscriptionPlan;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionPlan);
    }

}

