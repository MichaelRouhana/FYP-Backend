package com.example.AzureTestProject.Api.Model.Filter;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductionFilterDTO {
    private LocalDate date;
    private String staffNote;
    private Long quantity;
}
