package com.example.AzureTestProject.Api.Validation.Validator;


import com.example.AzureTestProject.Api.Validation.Annotation.Country;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Locale;

public class CountryValidator implements ConstraintValidator<Country, String> {

    @Override
    public boolean isValid(String countryCode, ConstraintValidatorContext context) {
        if (countryCode == null || countryCode.isEmpty()) {
            return false;
        }
        return Arrays.asList(Locale.getISOCountries()).contains(countryCode.toUpperCase());
    }
}
