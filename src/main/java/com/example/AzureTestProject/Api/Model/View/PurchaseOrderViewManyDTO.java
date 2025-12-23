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
public class PurchaseOrderViewManyDTO {


    private String uuid;
    private String date;
    private String status;
    private List<PurchaseOrderEntryViewDTO> items;
    private String supplier;
    private String paymentMode;
    private String supplierUUID;
    private BigDecimal totalCost;
    private BigDecimal discount;
    private boolean processed;
    private String note;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PurchaseOrderEntryViewDTO {
        private String product;
        private String productName;
        private long quantity;
        private long receivedQuantity;
        private BigDecimal cost;
    }


}
