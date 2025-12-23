package com.example.FYP.Api.Model.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AddressRequestDTO {

    @Size(max = 100, message = "Street must be up to 100 characters")
    private String street;


    @Size(max = 50, message = "City must be up to 50 characters")
    private String city;

    @Size(max = 10, message = "PinCode must be up to 10 characters")
    private String pinCode;

    @Size(max = 100, message = "Building must be up to 100 characters")
    private String building;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 50, message = "Country must be up to 50 characters")
    private String country;

    @Size(max = 50, message = "Zone must be up to 50 characters")
    private String zone;

    @Size(max = 50, message = "Stamping Zone must be up to 50 characters")
    private String stampingZone;
}
