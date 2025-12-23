package com.example.FYP.Api.Util;

import com.example.FYP.Api.Exception.ApiRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

public class ValidatorUtils {

    public static <T, D> void validateAndApplyFields(
            T entity,
            D dtoInstance,
            Map<String, Object> values,
            Class<T> entityClass,
            Class<D> dtoClass
    ) {

        // Set up the validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, entityClass.getSimpleName());

        // Loop through all values and apply them to the DTO and entity
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Find fields in DTO and entity by reflection
            Field dtoField = ReflectionUtils.findField(dtoClass, key);
            Field entityField = ReflectionUtils.findField(entityClass, key);

            if (dtoField != null && entityField != null) {
                dtoField.setAccessible(true);
                entityField.setAccessible(true);

                // Convert the value before setting it in the DTO field
                Object convertedValue = convertValue(dtoField.getType(), value);

                // Set the converted value in the DTO (first step)
                ReflectionUtils.setField(dtoField, dtoInstance, convertedValue);

                // Validate the field in the DTO
                Set<ConstraintViolation<D>> violations = validator.validateProperty(dtoInstance, key);

                // If no violations, apply the value to the entity
                if (violations.isEmpty()) {
                    Object entityConvertedValue = convertValue(entityField.getType(), convertedValue);
                    // Set the converted value on the entity field
                    ReflectionUtils.setField(entityField, entity, entityConvertedValue);
                } else {
                    // If there are violations, reject the value
                    violations.forEach(violation -> bindingResult.rejectValue(
                            key,
                            violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                            violation.getMessage()
                    ));
                }
            } else {
                throw new IllegalArgumentException("Field " + key + " does not exist in " + dtoClass.getSimpleName());
            }
        }

        // If there are any validation errors, throw an exception
        if (bindingResult.hasErrors()) {
            throw ApiRequestException.badRequest("Validation failed: " + bindingResult);
        }
    }


    private static Object convertValue(Class<?> targetType, Object value) {
        if (value == null) return null;

        // Handle LocalDate conversion from String
        if (targetType == LocalDate.class && value instanceof String) {
            try {
                return LocalDate.parse((String) value);  // Expected format: yyyy-MM-dd
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format for LocalDate: " + value);
            }
        }

        // Handle LocalDateTime conversion from String
        if (targetType == LocalDateTime.class && value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);  // Expected format: yyyy-MM-dd'T'HH:mm:ss
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date-time format for LocalDateTime: " + value);
            }
        }

        // Handle enum conversion (only for enum fields)
        if (targetType.isEnum() && value instanceof String stringValue) {
            try {
                return Enum.valueOf((Class<Enum>) targetType, stringValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid enum value '" + stringValue + "' for enum type " + targetType.getSimpleName());
            }
        }

        // Handle common type conversions
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value.toString());
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value.toString());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value.toString());
        }

        // Return the value if it's already of the correct type
        if (targetType.isInstance(value)) {
            return value;
        }

        // If we can't convert, return the value as it is (fallback)
        return value;
    }


}
