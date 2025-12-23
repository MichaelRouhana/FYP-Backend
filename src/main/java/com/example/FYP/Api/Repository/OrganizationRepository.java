package com.example.FYP.Api.Repository;


import com.example.FYP.Api.Entity.Organization;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Model.Response.OrganizationViewResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {

    boolean existsByOwner(User owner);

    Optional<Organization> findByUuid(String uuid);



}
