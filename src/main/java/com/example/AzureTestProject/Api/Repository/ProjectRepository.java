package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Optional<Project> findByUuid(String uuid);

    Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.uuid = :uuid AND p.organization.id = :organizationId")
    Optional<Project> findByUuidAndOrganizationId(@Param("uuid") String uuid, @Param("organizationId") Long organizationId);

    Optional<Project> findByOrganization_UuidAndUuid(String organizationUUID, String projectUUID);
}
