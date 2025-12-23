package com.example.AzureTestProject.Api.Model.Filter;

import com.example.AzureTestProject.Api.Model.Constant.Priority;
import com.example.AzureTestProject.Api.Validation.Annotation.EnumValidator;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MaterialRequisiteFilterDTO {

    @EnumValidator(enumClass = Priority.class, message = "Invalid priority. Allowed values are: ")
    private String priority;
    private LocalDate date;
    private LocalDate requiredDate;
    private String uuid;
}
