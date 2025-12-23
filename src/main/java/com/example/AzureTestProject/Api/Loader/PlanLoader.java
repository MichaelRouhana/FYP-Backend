package com.example.AzureTestProject.Api.Loader;


import com.example.AzureTestProject.Api.Entity.Feature;
import com.example.AzureTestProject.Api.Entity.Plan;
import com.example.AzureTestProject.Api.Repository.FeatureRepository;
import com.example.AzureTestProject.Api.Repository.PlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class PlanLoader {

    private final FeatureRepository featureRepository;
    private final PlanRepository planRepository;

    public PlanLoader(FeatureRepository featureRepository, PlanRepository planRepository) {
        this.featureRepository = featureRepository;
        this.planRepository = planRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void loadPlans() {
        if (planRepository.count() > 0) {
            log.info("Plans already exist, skipping creation.");
            return;
        }

        List<Feature> allFeatures = featureRepository.findAll();

        Set<Feature> freeFeatures = new HashSet<>();
        for (Feature feature : allFeatures) {
            if (!feature.getName().equalsIgnoreCase("ApprovalController")) {
                freeFeatures.add(feature);
            }
        }

        Set<Feature> enterpriseFeatures = new HashSet<>(allFeatures);

        Plan freePlan = new Plan();
        freePlan.setName("Free");
        freePlan.setPrice(BigDecimal.ZERO);
        freePlan.setDurationInDays(365);
        freePlan.setSubscriptionPlan(Plan.SubscriptionPlan.FREE);
        freePlan.setFeatures(freeFeatures);

        Plan enterprisePlan = new Plan();
        enterprisePlan.setName("Enterprise");
        enterprisePlan.setPrice(new BigDecimal("49.99"));
        enterprisePlan.setDurationInDays(30);
        enterprisePlan.setSubscriptionPlan(Plan.SubscriptionPlan.ENTERPRISE);
        enterprisePlan.setFeatures(enterpriseFeatures);

        planRepository.saveAll(List.of(freePlan, enterprisePlan));

        log.info("Plans created: Free and Enterprise!");
    }
}
