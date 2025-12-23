package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByOrganization_Uuid(String organizationUuid);

}
