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
public class PurchaseOrderViewOneDTO {


    private String uuid;
    private boolean processed;
    private String date;
    private String status;
    private Boolean moveToDirectCost;
    private String supplier;
    private String supplierUUID;
    private BigDecimal discount;
    private BigDecimal totalCost;
    private String paymentMode;
    private String trn;
    private String site;
    private String purchaseRequestUUID;
    private List<PurchaseOrderEntryViewDTO> items;
    private String note;


    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PurchaseOrderEntryViewDTO {
        private String uuid;
        private String name;
        private long quantity;
        private long receivedQuantity;
        private BigDecimal cost;
    }


}
