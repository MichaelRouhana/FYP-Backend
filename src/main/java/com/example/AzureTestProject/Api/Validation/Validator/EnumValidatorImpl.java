package com.example.AzureTestProject.Api.Validation.Validator;

import com.example.AzureTestProject.Api.Validation.Annotation.EnumValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

    private Class<? extends Enum<?>> enumClass;
    private String validValues;
    private String message;

    @Override
    public void initialize(EnumValidator annotation) {
        this.message = annotation.message();
        this.enumClass = annotation.enumClass();
        this.validValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean isValid = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .anyMatch(enumValue -> enumValue.equals(value));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    message + " " + validValues
            ).addConstraintViolation();
        }

        return isValid;
    }
}
