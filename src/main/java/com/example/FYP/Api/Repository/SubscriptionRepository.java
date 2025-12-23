package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByOrganization_Uuid(String organizationUuid);

}
