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
public class ProductViewDTO {
    private String uuid;
    private String code;
    private String name;
    private String icon;
    private String category;
    private String subCategory;
    private String brand;
    private String type;
    private BigDecimal price;
    private String unit;
    private String supplier;
    private String supplierUUID;
    private String details;
    private String status;
    private long totalQuantity;
    private List<InventoryViewDTO> inventories;

    @Data
    public static class InventoryViewDTO {
        private long quantity;
        private BigDecimal cost;
        private Boolean expires;
        private String expiryDate;

    }
}
