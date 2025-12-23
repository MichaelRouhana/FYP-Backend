package com.example.AzureTestProject.Api.Model.Patch;

import lombok.Data;

@Data
public class AssetPatchDTO {
    private String depreciationMode;
    private String depreciationPeriod;
    private String note;
}
