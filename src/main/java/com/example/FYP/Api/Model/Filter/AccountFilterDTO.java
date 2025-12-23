package com.example.FYP.Api.Model.Filter;

import lombok.Data;

import java.util.List;

@Data
public class AccountFilterDTO {

    private String name;
    private List<String> mainType;
    private List<String> subType;
    private List<String> accountType;
    private String additionalInfo;
}
