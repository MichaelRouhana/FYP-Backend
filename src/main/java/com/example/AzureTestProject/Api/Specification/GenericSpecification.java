package com.example.AzureTestProject.Api.Specification;

import com.example.AzureTestProject.Api.Exception.ApiRequestException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GenericSpecification<T> {

    public static <T> Specification<T> filterByFields(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((field, value) -> {
                if (value != null) {
                    try {
                        if (!List.of("page", "size", "sortBy", "sortDir").contains(field.toLowerCase())) {
                            if (value instanceof Collection<?>) {
                                predicates.add(root.get(field).in((Collection<?>) value));
                            } else {
                                predicates.add(criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get(field).as(String.class)),
                                        "%" + value.toString().toLowerCase() + "%"
                                ));
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        throw ApiRequestException.badRequest("Error filtering: " + e.getMessage());
                    }
                }
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static <T> Specification<T> filterByFields(Object filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (Field field : filter.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(filter);
                    if (value != null) {
                        String fieldName = field.getName();
                        if (value instanceof Collection<?>) {
                            predicates.add(root.get(fieldName).in((Collection<?>) value));
                        } else if (value instanceof String) {
                            predicates.add(criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(fieldName)),
                                    "%" + value.toString().toLowerCase() + "%"
                            ));
                        } else {
                            predicates.add(criteriaBuilder.equal(root.get(fieldName), value));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw ApiRequestException.badRequest("Error accessing field: " + field.getName());
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}