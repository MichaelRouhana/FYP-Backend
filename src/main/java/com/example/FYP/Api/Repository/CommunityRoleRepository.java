package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.CommunityRole;
import com.example.FYP.Api.Model.Constant.CommunityRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRoleRepository extends JpaRepository<CommunityRole, Long> {
    List<CommunityRole> findByRoleIn(List<CommunityRoles> roles);

    @Query("""
    SELECT r.role
    FROM CommunityRole r
    JOIN r.users u
    WHERE u.email = :email AND r.community.id = :communityId
""")
    List<String> findRolesByUserAndCommunity(@Param("email") String email,
                                                     @Param("communityId") Long communityId);

    @Query("""
    SELECT r
    FROM CommunityRole r
    WHERE r.community.id = :communityId AND r.role = :role
""")
    List<CommunityRole> findByCommunityIdAndRole(@Param("communityId") Long communityId,
                                                  @Param("role") CommunityRoles role);

}