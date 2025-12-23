package com.example.AzureTestProject.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrnViewDTO {

    private String uuid;
    private String purchaseOrder;
    private String wareHouse;
    private String wareHouseUUID;
    private String supplier;
    private String supplierUUID;
    private String vat;
    private String date;
    private BigDecimal totalCost;
    private BigDecimal discount;
    private boolean processed;
    private List<GrnOrderViewDTO> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GrnOrderViewDTO {
        private String uuid;
        private String name;
        private Boolean expires;
        private String expiryDate;
        private BigDecimal cost;
        private String unit;
        private long receivedQuantity;
        private long quantity;

    }
}
