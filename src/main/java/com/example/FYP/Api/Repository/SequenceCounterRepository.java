package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.SequenceCounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceCounterRepository extends JpaRepository<SequenceCounter, Long> {
}
