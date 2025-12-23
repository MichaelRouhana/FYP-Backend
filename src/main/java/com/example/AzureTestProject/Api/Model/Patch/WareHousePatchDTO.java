package com.example.AzureTestProject.Api.Model.Patch;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WareHousePatchDTO {

    private String name;

    private String city;

    private String country;

    private String street;

    private String pinCode;

    @Size(max = 200, message = "Details must not exceed 500 characters")
    private String note;
}
