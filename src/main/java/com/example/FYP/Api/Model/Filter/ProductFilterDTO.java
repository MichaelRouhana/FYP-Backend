package com.example.FYP.Api.Model.Filter;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductFilterDTO {
    private Long id;
    private String uuid;
    private String name;
    private List<String> type;
    private String symbology;
    private LocalDate expiryDate;
    private Long price;
    private Long unit;
    private List<String> status;
    private String supplier;
    private String details;
}
