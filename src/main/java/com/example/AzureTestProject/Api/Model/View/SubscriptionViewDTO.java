package com.example.AzureTestProject.Api.Model.View;

import lombok.Data;

@Data
public class SubscriptionViewDTO {
    private String startDate;
    private String endDate;
    private String status;
    private String plan;
}
