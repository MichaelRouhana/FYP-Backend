package com.example.FYP.Api.Model.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressViewDTO {

    private String street;
    private String city;
    private String pinCode;
    private String building;
    private String country;
    private String zone;
    private String stampingZone;
}
