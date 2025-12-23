package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
}
