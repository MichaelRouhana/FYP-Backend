package com.example.FYP.Api.Model.Filter;


import lombok.Data;

import java.time.LocalDate;

@Data
public class GrnFilterDTO {
    public Boolean processed;
    private String poReferenceNo;
    private String vat;
    private String wareHouseUUID;
    private LocalDate date;
}
