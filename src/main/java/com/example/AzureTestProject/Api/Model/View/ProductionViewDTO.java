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
public class  ProductionViewDTO {

    private String uuid;
    private String date;
    private long quantity;
    private String warehouseName;
    private String warehouseUUID;
    private String staffNote;
    private String finishedGoodUUID;
    private String finishedGoodName;
    private String unit;
    private List<ProductionEntryViewDTO> entries;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductionEntryViewDTO {
        private String uuid;
        private String name;
        private long quantity;
        private String unit;
        private BigDecimal cost;

    }
}
