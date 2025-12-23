package com.example.FYP.Api.Model.View;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanViewDTO {
    private String name;
    private BigDecimal price;
    private Integer durationInDays;
    private String subscriptionPlan;
}
