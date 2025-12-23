package com.example.AzureTestProject.Api.Serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;

import java.io.IOException;

public class GenericEnumDeserializers extends SimpleDeserializers {
    @Override
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                    DeserializationConfig config,
                                                    BeanDescription beanDesc) {
        if (type.isEnum()) {
            return new JsonDeserializer<Enum<?>>() {
                @Override
                public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getText();
                    Class<Enum> enumType = (Class<Enum>) type;

                    try {
                        int ordinal = Integer.parseInt(value);
                        Enum<?>[] constants = enumType.getEnumConstants();
                        if (ordinal >= 0 && ordinal < constants.length) {
                            return constants[ordinal];
                        }
                        throw new InvalidFormatException(p, "Invalid ordinal for enum " + enumType.getSimpleName(), value, enumType);
                    } catch (NumberFormatException | InvalidFormatException ignored) {
                    }

                    for (Enum<?> constant : enumType.getEnumConstants()) {
                        if (constant.name().equalsIgnoreCase(value)) {
                            return constant;
                        }
                    }

                    throw new InvalidFormatException(p, "Cannot deserialize to enum " + enumType.getSimpleName(), value, enumType);
                }
            };
        }
        return null;
    }
}
