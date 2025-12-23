package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureRepository  extends JpaRepository<Feature, Long> {
    List<Feature> findByPatternIn(List<String> patterns);
}
