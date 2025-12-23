package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.OrganizationRole;
import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRoleRepository extends JpaRepository<OrganizationRole, Long> {
    List<OrganizationRole> findByRoleIn(List<OrganizationRoles> roles);


    @Query("SELECT r.role FROM OrganizationRole r " +
            "JOIN r.users u " +
            "JOIN r.organization o " +
            "WHERE u.email = :email AND o.uuid = :organizationUUID")
    List<String> findRolesByUserAndOrganization(@Param("email") String email,
                                                @Param("organizationUUID") String organizationUUID);
}