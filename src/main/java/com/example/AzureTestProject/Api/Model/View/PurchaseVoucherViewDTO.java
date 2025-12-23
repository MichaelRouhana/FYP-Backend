package com.example.AzureTestProject.Api.Model.View;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseVoucherViewDTO extends VoucherViewDTO {
    private String grnUUID;
    private String dueDate;
    private String vat;
    private String supplier;
    private String supplierUUID;
    private BigDecimal amountLeft;
    private BigDecimal discount;
    private Boolean processed;
    private List<VoucherProductViewDTO> items;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoucherProductViewDTO {
        private String uuid;
        private String name;
        private Boolean expires;
        private LocalDate expiryDate;
        private BigDecimal cost;
        private long receivedQuantity;
    }
}
