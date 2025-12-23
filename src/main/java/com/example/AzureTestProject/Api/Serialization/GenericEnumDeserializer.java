package com.example.AzureTestProject.Api.Serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;

public class GenericEnumDeserializer<T extends Enum<T>> extends JsonDeserializer<T> {

    private final Class<T> enumType;

    public GenericEnumDeserializer(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();

        try {
            int ordinal = Integer.parseInt(value);
            T[] constants = enumType.getEnumConstants();
            if (ordinal >= 0 && ordinal < constants.length) {
                return constants[ordinal];
            }
            throw new InvalidFormatException(p, "Invalid ordinal for enum " + enumType.getSimpleName(), value, enumType);
        } catch (NumberFormatException ignored) {
        }

        for (T constant : enumType.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }

        throw new InvalidFormatException(p, "Cannot deserialize to enum " + enumType.getSimpleName(), value, enumType);
    }
}
