package com.example.AzureTestProject.Api.Service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class CountryService {
    private static final String EXCHANGE_RATE_API_KEY = "eb9601b05cfa396416924cce";
    private static final String VAT_LAYER_API_KEY = "your-vat-layer-api-key";

    private static final String EXCHANGE_RATE_URL = "https://v6.exchangerate-api.com/v6/" + EXCHANGE_RATE_API_KEY + "/latest/";
    private static final String VAT_LAYER_URL = "https://api.vatlayer.com/validate?access_key=" + VAT_LAYER_API_KEY + "&country_code=";

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static void main(String[] args) throws Exception {
        System.out.println(fetchCurrency("LB"));

    }

    public static Map<String, Object> getCurrencyAndVAT(String countryCode) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // Fetch currency using ExchangeRate API
        String currency = fetchCurrency(countryCode);
        result.put("currency", currency);

        // Fetch VAT using VAT Layer API
        double vat = fetchVAT(countryCode);
        result.put("vat", vat);

        return result;
    }

    private static String fetchCurrency(String countryCode) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EXCHANGE_RATE_URL + countryCode))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonResponse = objectMapper.readTree(response.body());

        if (jsonResponse.has("conversion_rates")) {
            return jsonResponse.get("conversion_rates").get("USD").asText(); // Example: USD
        }
        return "Unknown Currency";
    }

    private static double fetchVAT(String countryCode) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VAT_LAYER_URL + countryCode))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonResponse = objectMapper.readTree(response.body());

        if (jsonResponse.has("valid") && jsonResponse.get("valid").asBoolean()) {
            return jsonResponse.get("vat_rate").asDouble(); // Example: VAT rate
        }
        return 0.0;
    }
}
