package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    // Find all communities where the user is a member
    List<Community> findByUsers_Id(Long userId);
    
    // Check if a community with the given name already exists
    boolean existsByName(String name);
    
    // Find a community by name (optional, for validation)
    java.util.Optional<Community> findByName(String name);
    
    // Find a community by invite code (for QR code invitations)
    java.util.Optional<Community> findByInviteCode(String inviteCode);
    
    /**
     * Native query to update member role directly in community_users table
     * This bypasses JPA mapping issues for the join table
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE community_users SET role = :role WHERE community_id = :communityId AND user_id = :userId", nativeQuery = true)
    void updateMemberRole(@Param("communityId") Long communityId, @Param("userId") Long userId, @Param("role") String role);
    
    /**
     * Check if a user is a member of a community (SQL-driven, bypasses JPA mapping)
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM community_users WHERE community_id = :communityId AND user_id = :userId", nativeQuery = true)
    boolean existsByCommunityIdAndUserId(@Param("communityId") Long communityId, @Param("userId") Long userId);
}
