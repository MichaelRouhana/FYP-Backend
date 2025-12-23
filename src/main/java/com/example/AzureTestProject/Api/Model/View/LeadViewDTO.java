package com.example.AzureTestProject.Api.Model.View;

import com.example.AzureTestProject.Api.Model.Constant.Industry;
import com.example.AzureTestProject.Api.Model.Constant.LeadStatus;
import com.example.AzureTestProject.Api.Model.Response.AddressViewDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadViewDTO {

    private String uuid;
    private String name;
    private String contactName;
    private Industry industry;
    private LeadStatus status;
    private String trn;
    private String email;
    private String website;
    private String numberOfEmployees;
    private String number;
    private String extension;
    private AddressViewDTO address;
    private String secondaryNumber;
    private String secondaryNumberExtension;

}
