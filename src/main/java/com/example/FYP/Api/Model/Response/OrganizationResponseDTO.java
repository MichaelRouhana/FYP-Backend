package com.example.FYP.Api.Model.Response;

import com.example.FYP.Api.Model.Constant.BuisnessType;
import com.example.FYP.Api.Model.Constant.OrganizationType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganizationResponseDTO {
    private String uuid;
    private String icon;
    private String name;
    private String phoneNumber;
    private String trn;
    private BuisnessType businessType;
    private String email;
    private OrganizationType organizationType;
    private String license;
    private String expiryDate;
    private String establishmentDate;
    private String description;
    private String currency;
    private String country;
    private String costMethod;
    private BigDecimal vat;
    private BigDecimal shares;

}
