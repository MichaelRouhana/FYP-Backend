package com.example.FYP.Api.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "feature")
@Data
public class Feature {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String pattern;
}