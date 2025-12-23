package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.SequenceCounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceCounterRepository extends JpaRepository<SequenceCounter, Long> {
}
