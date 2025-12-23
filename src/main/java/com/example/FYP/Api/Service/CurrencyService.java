package com.example.FYP.Api.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CurrencyService {

    private static final String CURRENCY_API_URL = "https://openexchangerates.org/api/currencies.json?app_id=7eae525358bc41cb96023fcc60198b8a";

    public boolean isValidCurrency(String currencyCode) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> currencies = restTemplate.getForObject(CURRENCY_API_URL, Map.class);

        return currencies != null && currencies.containsKey(currencyCode.toUpperCase());
    }
}
