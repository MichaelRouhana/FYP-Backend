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
public class SaleViewDTO {

    private String uuid;
    private String status;
    private String date;
    private String dueDate;
    private String vat;
    private BigDecimal discount;
    private String shippingMethod;
    private String paymentMode;
    private String address;
    private String paymentNote;
    private String wareHouse;
    private String warehouseUUID;
    private String customerName;
    private String customerUUID;
    private boolean processed;
    private BigDecimal cogs;
    private BigDecimal amountLeft;
    private List<SaleOrderViewDTO> saleOrders;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SaleOrderViewDTO {
        private String uuid;
        private String name;
        private long quantity;
        private long price;

    }
}
