package com.example.AzureTestProject.Api.Model.Patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchSettingsPatchDTO {
    private Boolean allowBettingHT;
    private Boolean showMatch;
    private Boolean allowBetting;

}
