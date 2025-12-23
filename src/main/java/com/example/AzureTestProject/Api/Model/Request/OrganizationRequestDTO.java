package com.example.AzureTestProject.Api.Model.Request;


import com.example.AzureTestProject.Api.Model.Constant.BuisnessType;
import com.example.AzureTestProject.Api.Model.Constant.OrganizationType;
import com.example.AzureTestProject.Api.Validation.Annotation.Country;
import com.example.AzureTestProject.Api.Validation.Annotation.Currency;
import com.example.AzureTestProject.Api.Validation.Annotation.EnumValidator;
import com.example.AzureTestProject.Api.Validation.Annotation.ValidTRN;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationRequestDTO {
    private String icon;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 8, max = 100, message = "Name must be between 8 and 100 characters")
    private String name;

    @NotBlank(message = "country cannot be blank")
    @Country
    private String country;

    @NotBlank(message = "currency cannot be blank")
    @Currency
    private String currency;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(min = 8, max = 16, message = "Phone number must be between 8 and 16 characters")
    private String phoneNumber;

    @NotBlank(message = "TRN cannot be blank")
    @ValidTRN
    private String trn;

    @NotBlank(message = "Business type cannot be null")
    @EnumValidator(
            enumClass = BuisnessType.class,
            message = "Invalid businessType. Allowed values are: "
    )
    private String businessType;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "email wrong format")
    private String email;

    @NotBlank(message = "organizationType cannot be null")
    @EnumValidator(
            enumClass = OrganizationType.class,
            message = "Invalid organizationType. Allowed values are: "
    )
    private String organizationType;

    @NotBlank(message = "license cannot be blank")
    @Size(min = 8, max = 16, message = "license must be between 8 and 16 characters")
    private String license;

    @NotNull(message = "Expiry date cannot be blank")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Establishment date cannot be blank")
    @PastOrPresent(message = "Establishment date must be in the past")
    private LocalDate establishmentDate;




    @Size(min = 8, max = 200, message = "Description must be between 8 and 200 characters")
    private String description;

    @NotNull(message = "vat cannot be null")
    @PositiveOrZero
    private BigDecimal vat;

    @NotNull(message = "shares cannot be null")
    private BigDecimal shares;
}
