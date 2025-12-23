package com.example.FYP.Api.Model.View;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSheetViewAllDTO {
    private String uuid;
    private String date;
    private String name;


}
