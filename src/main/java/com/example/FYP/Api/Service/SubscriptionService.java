package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Organization;
import com.example.FYP.Api.Entity.Plan;
import com.example.FYP.Api.Entity.Subscription;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Mapper.SubscriptionMapper;
import com.example.FYP.Api.Model.View.SubscriptionViewDTO;
import com.example.FYP.Api.Repository.OrganizationRepository;
import com.example.FYP.Api.Repository.PlanRepository;
import com.example.FYP.Api.Repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final OrganizationRepository organizationRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public void subscribe(Organization organization, Plan plan){
        if(organization.getSubscription() != null && organization.getSubscription().getPlan() == plan) throw ApiRequestException.badRequest("Already subscribed to this plan");


        if(organization.getSubscription() != null) subscriptionRepository.delete(organization.getSubscription());

        Subscription subscription = Subscription.builder()
                .organization(organization)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(plan.getDurationInDays()))
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .plan(plan)
                .build();

        subscriptionRepository.save(subscription);

    }

    public void subscribe(String organizationUUID, Plan.SubscriptionPlan subscriptionPlan){
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found : " + organizationUUID));
        Plan plan = getPlan(subscriptionPlan);
        subscribe(organization, plan);
    }


    public void subscribe(Organization organization, Plan.SubscriptionPlan subscriptionPlan){
        Plan plan = getPlan(subscriptionPlan);
        subscribe(organization, plan);
    }

    @Cacheable("plans")
    public Plan getPlan(Plan.SubscriptionPlan subscriptionPlan) {
        return planRepository.findBySubscriptionPlan(subscriptionPlan)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found for: " + subscriptionPlan));
    }

    public SubscriptionViewDTO my(@NotBlank(message = "organizationUUID cannot be blank") String organizationUUID) {
        return subscriptionRepository.findByOrganization_Uuid(organizationUUID)
                .map(subscriptionMapper::toViewDTO)
                .orElseThrow(() -> new EntityNotFoundException("subscription not found"));
    }
}
