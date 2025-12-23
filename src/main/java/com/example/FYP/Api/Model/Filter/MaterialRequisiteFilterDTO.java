package com.example.FYP.Api.Model.Filter;

import com.example.FYP.Api.Model.Constant.Priority;
import com.example.FYP.Api.Validation.Annotation.EnumValidator;
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
