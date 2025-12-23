package com.example.FYP.Api.Model.Filter;

import lombok.Data;

@Data
public class ProjectFilterDTO {
    private String name;
    private String type;
    private String projectStatus;
    private String prmReferenceNo;
    private String clientReferenceNo;
    private String clientName;
    private String clientCountry;
    private String clientAddress;
    private String clientPhoneNumber;
    private String clientEmail;
    private String attachment;
    private String description;
}
