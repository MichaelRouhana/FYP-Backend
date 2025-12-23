package com.example.AzureTestProject.Api.Model.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WareHouseRequestDTO {

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "city cannot be blank")
    private String city;

    @NotBlank(message = "country cannot be blank")
    private String country;

    @NotBlank(message = "street cannot be blank")
    private String street;

    private String pinCode;

    @Size(max = 200, message = "Details must not exceed 500 characters")
    private String note;
}
