package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository  extends JpaRepository<Fixture, Long> {
}
