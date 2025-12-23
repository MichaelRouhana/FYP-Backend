package com.example.AzureTestProject.Api.Validation.Annotation;


import com.example.AzureTestProject.Api.Validation.Validator.CountryValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CountryValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Country {
    String message() default "Invalid country code. Use ISO 3166-1 alpha-2 codes (e.g., US, FR, DE)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
