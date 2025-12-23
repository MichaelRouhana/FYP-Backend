package com.example.AzureTestProject.Api.Validation.Validator;

import com.example.AzureTestProject.Api.Validation.Annotation.ValidTRN;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TRNValidator implements ConstraintValidator<ValidTRN, String> {
    private static final String TRN_REGEX = "^[0-9]{15}$"; // 15-digit TRN

    @Override
    public boolean isValid(String trn, ConstraintValidatorContext context) {
        return trn != null && trn.matches(TRN_REGEX);
    }
}
