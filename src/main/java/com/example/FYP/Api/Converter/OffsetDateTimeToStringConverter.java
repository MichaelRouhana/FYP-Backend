package com.example.FYP.Api.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class OffsetDateTimeToStringConverter implements AttributeConverter<OffsetDateTime, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public String convertToDatabaseColumn(OffsetDateTime attribute) {
        if (attribute == null) return null;
        return attribute.format(FORMATTER);
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return OffsetDateTime.parse(dbData, FORMATTER);
    }
}
