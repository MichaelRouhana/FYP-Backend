package com.example.FYP.Api.Model.Patch;

import com.example.FYP.Api.Model.Constant.BuisnessType;
import com.example.FYP.Api.Model.Constant.OrganizationType;
import com.example.FYP.Api.Validation.Annotation.Country;
import com.example.FYP.Api.Validation.Annotation.Currency;
import com.example.FYP.Api.Validation.Annotation.EnumValidator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganizationPatchDTO {

    @Size(min = 8, max = 100, message = "Name must be between 8 and 100 characters")
    private String name;

    @Size(min = 8, max = 16, message = "Phone number must be between 8 and 16 characters")
    private String phoneNumber;

    @EnumValidator(enumClass = BuisnessType.class, message = "Invalid businessType. Allowed values are: ")
    private String businessType;

    @Email(message = "Email should be valid")
    private String email;

    @EnumValidator(enumClass = OrganizationType.class, message = "Invalid organizationType. Allowed values are: ")
    private String organizationType;

    @Size(min = 8, max = 16, message = "License must be between 8 and 16 characters")
    private String license;

    @Country
    private String country;

    @Currency
    private String currency;


    @Size(min = 8, max = 200, message = "Description must be between 8 and 200 characters")
    private String description;

    private BigDecimal vat;

}
