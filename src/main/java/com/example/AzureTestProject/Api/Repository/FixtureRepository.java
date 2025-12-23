package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository  extends JpaRepository<Fixture, Long> {
}
