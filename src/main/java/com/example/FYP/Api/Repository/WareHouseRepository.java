package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, Long>, JpaSpecificationExecutor<WareHouse> {

    Optional<WareHouse> findByUuid(String uuid);

    @Query("SELECT p FROM WareHouse p WHERE p.uuid = :wareHouseUuid AND p.organization.id = :organization_id")
    Optional<WareHouse> findByOrganization(@Param("wareHouseUuid") String wareHouseUuid, @Param("organization_id") long organization_id);

    List<WareHouse> findByOrganizationUuid(String organizationUUID);

    Optional<WareHouse> findByOrganization_UuidAndUuid(String organizationUUID, String wareHouseUUID);

    @Query("SELECT w FROM WareHouse w WHERE w.organization.id = :orgId AND w.defaultWareHouse = true")
    Optional<WareHouse> findDefaultByOrganizationId(@Param("orgId") Long orgId);


}
