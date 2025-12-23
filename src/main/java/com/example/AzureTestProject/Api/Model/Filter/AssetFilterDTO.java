package com.example.AzureTestProject.Api.Model.Filter;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetFilterDTO {
    private String name;
    private String depreciationMode;
    private String depreciationPeriod;
    private BigDecimal depreciationValueMin;
    private BigDecimal depreciationValueMax;
    private String projectUUID;
}
