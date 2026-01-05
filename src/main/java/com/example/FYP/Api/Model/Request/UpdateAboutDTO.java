package com.example.FYP.Api.Model.Request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAboutDTO {
    @Size(max = 1000, message = "About section must be at most 1000 characters")
    private String about;
}

