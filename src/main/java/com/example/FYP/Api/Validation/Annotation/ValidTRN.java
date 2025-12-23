package com.example.FYP.Api.Validation.Annotation;

import com.example.FYP.Api.Validation.Validator.TRNValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TRNValidator.class)
public @interface ValidTRN {
    String message() default "Invalid TRN number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
