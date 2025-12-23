package com.example.FYP.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseRequestViewAllDTO {

    private String uuid;
    private String materialRequisite;
    private String supplier;
    private String supplierName;
    private boolean processed;

}

