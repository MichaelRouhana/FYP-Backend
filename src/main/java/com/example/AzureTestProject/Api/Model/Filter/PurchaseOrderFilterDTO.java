package com.example.AzureTestProject.Api.Model.Filter;

import lombok.Data;

import java.time.LocalDate;

@Data

public class PurchaseOrderFilterDTO {
    public Boolean processed;
    private LocalDate date;
    private String po;
    private String trn;
    private String site;
    private String status;
    private String paymentMode;
    private String note;
    private Boolean moveToDirectCost;
}
