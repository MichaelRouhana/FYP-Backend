package com.example.FYP.Api.Model.Filter;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSheetFilterDTO {
    private LocalDate date;
    private LocalTime time;
}
