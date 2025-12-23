package com.example.FYP.Api.Validation.Validator;

import com.example.FYP.Api.Validation.Annotation.Currency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CurrencyValidator implements ConstraintValidator<Currency, String> {

    private static final String CURRENCY_API_URL = "https://openexchangerates.org/api/currencies.json?app_id=7eae525358bc41cb96023fcc60198b8a";

    @Override
    public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> currencies = restTemplate.getForObject(CURRENCY_API_URL, Map.class);

        return currencyCode != null && currencies != null && currencies.containsKey(currencyCode.toUpperCase());
    }
}