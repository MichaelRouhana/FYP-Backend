package com.example.AzureTestProject.Api.Repository;


import com.example.AzureTestProject.Api.Entity.Organization;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Model.Response.OrganizationViewResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {
    boolean existsByUuid(String uuid);

    boolean existsByOwner(User owner);

    Optional<Organization> findByUuid(String uuid);

    @Query("SELECT new com.example.AzureTestProject.Api.Model.Response.OrganizationViewResponseDTO( " +
            "o.uuid, o.name, COUNT(DISTINCT u), COUNT(DISTINCT p), o.owner.email, o.vat, o.shares) " +
            "FROM Organization o " +
            "JOIN o.users u " +
            "LEFT JOIN o.projects p " +
            "WHERE u.email = :userEmail " +
            "GROUP BY o.uuid, o.name, o.owner.email, o.vat, o.shares")
    List<OrganizationViewResponseDTO> getOrganizationViewsByUser(@Param("userEmail") String userEmail);


}
