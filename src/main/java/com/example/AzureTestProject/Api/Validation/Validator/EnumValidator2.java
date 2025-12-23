package com.example.AzureTestProject.Api.Validation.Validator;

import com.example.AzureTestProject.Api.Validation.Annotation.ValidEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator2 implements ConstraintValidator<ValidEnum, Object> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidEnum annotation) {
        this.enumClass = annotation.enumClass();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        System.out.println(value);
        if (value == null) {
            return true;  // or false if you want to disallow null
        }

        if (!enumClass.isInstance(value)) {
            return false;
        }

        // Check if the enum constants contain this value
        Object[] enumConstants = enumClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            if (enumConstant.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
