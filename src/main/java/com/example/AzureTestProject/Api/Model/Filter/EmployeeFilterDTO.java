package com.example.AzureTestProject.Api.Model.Filter;

import lombok.Data;

@Data
public class EmployeeFilterDTO {
    private String fullname;
    private String designation;
    private String workDays;
    private String salaryType;
    private Double salary;
    private String salaryCurrency;
    private Double insurance;
    private Double accomodation;
    private Double transportation;
    private Double visaExpense;
    private Double otherExpenses;
}
