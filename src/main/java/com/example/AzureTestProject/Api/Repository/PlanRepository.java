package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findBySubscriptionPlan(Plan.SubscriptionPlan subscriptionPlan);
}
