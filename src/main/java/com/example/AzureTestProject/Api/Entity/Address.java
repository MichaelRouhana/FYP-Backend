package com.example.AzureTestProject.Api.Entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Address {

    private String street;
    private String city;
    private String pinCode;
    private String building;
    private String country;
    private String zone;
    private String stampingZone;
}
