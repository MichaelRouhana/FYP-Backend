package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
}
