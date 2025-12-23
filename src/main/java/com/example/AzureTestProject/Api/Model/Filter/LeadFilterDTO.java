package com.example.AzureTestProject.Api.Model.Filter;

import lombok.Data;

@Data
public class LeadFilterDTO {
    private String title;
    private String name;
    private String company;
    private String industry;
    private Long numberOfEmployees;
    private String status;
    private String trn;
    private String address;
    private String email;
    private String website;
    private String secondaryNumber;
}
