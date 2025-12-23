package com.example.FYP.Api.Model.View;

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
public class GrnViewAllDTO {
    private String uuid;
    private String purchaseOrder;
    private String wareHouse;
    private String wareHouseUUID;
    private BigDecimal vat;
    private String date;
    private boolean processed;
    private BigDecimal discount;
    private String supplier;
    private String supplierUUID;
    private List<GrnProductViewDTO> items;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GrnProductViewDTO {
        private String product;
        private String productUUID;
        private Boolean expires;
        private String expiryDate;
        private long receivedQuantity;
        private long quantity;
    }

}
