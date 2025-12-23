package com.example.AzureTestProject.Api.Model.Filter;

import lombok.Data;

@Data
public class SupplierFilterDTO {


    private String name;
    private String supplierType;
    private String trn;
    private String email;
    private String website;
    private String number;
    private String numberOfEmployees;
    private String note;
}
