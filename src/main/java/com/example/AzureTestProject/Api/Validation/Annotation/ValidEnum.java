package com.example.AzureTestProject.Api.Validation.Annotation;

import com.example.AzureTestProject.Api.Validation.Validator.EnumValidator2;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator2.class)

@Documented
public @interface ValidEnum {
    String message() default "must be a valid enum value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();
}
