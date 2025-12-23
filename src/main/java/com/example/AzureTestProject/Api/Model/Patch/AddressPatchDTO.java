package com.example.AzureTestProject.Api.Model.Patch;


import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressPatchDTO {

    @Size(max = 100, message = "Street must be up to 100 characters")
    private String street;

    @Size(max = 100, message = "Area must be up to 100 characters")
    private String area;

    @Size(max = 50, message = "City must be up to 50 characters")
    private String city;

    @Size(max = 10, message = "PinCode must be up to 10 characters")
    private String pinCode;

    @Size(max = 100, message = "Building must be up to 100 characters")
    private String building;

    @Size(max = 50, message = "Country must be up to 50 characters")
    private String country;

    @Size(max = 50, message = "Zone must be up to 50 characters")
    private String zone;

    @Size(max = 50, message = "Stamping Zone must be up to 50 characters")
    private String stampingZone;
}
