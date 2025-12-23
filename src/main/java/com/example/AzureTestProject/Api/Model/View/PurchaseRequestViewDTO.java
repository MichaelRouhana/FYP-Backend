package com.example.AzureTestProject.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseRequestViewDTO {

    private String uuid;
    private String materialRequisite;
    private String supplier;
    private String supplierName;
    private boolean processed;


    private List<CategoryDTO> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CategoryDTO {
        private String category;
        private List<SubCategoryDTO> subCategories;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SubCategoryDTO {
        private String subCategory;
        private List<ItemViewDTO> items;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ItemViewDTO {
        private String name;
        private String uuid;
        private String unit;
        private long processedQuantity;
        private long quantity;
    }
}
