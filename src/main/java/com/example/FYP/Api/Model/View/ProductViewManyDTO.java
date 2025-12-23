package com.example.FYP.Api.Model.View;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewManyDTO {
    private String uuid;
    private String code;
    private String name;
    private String icon;
    private String brand;
    private String category;
    private String subCategory;
    private String status;
    private String type;
    private String unit;
    private String details;
    private long totalQuantity;
}
