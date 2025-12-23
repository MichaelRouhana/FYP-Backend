package com.example.AzureTestProject.Api.Validation.Annotation;

import com.example.AzureTestProject.Api.Validation.Validator.EnumValidatorImpl;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidatorImpl.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValidator {
    String message() default "Invalid value. Must be one of the enum values.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass(); // The enum class to validate against
}
