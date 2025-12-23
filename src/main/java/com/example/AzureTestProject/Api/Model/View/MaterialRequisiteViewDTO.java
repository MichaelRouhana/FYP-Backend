package com.example.AzureTestProject.Api.Model.View;

import com.example.AzureTestProject.Api.Model.Constant.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialRequisiteViewDTO {

    private String uuid;
    private Set<String> categories;
    private Set<String> subCategories;
    private String date;
    private String requiredDate;
    private Priority priority;
    private boolean processed;
    private String note;
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
        private long quantity;
        private long processedQuantity;
    }
}
