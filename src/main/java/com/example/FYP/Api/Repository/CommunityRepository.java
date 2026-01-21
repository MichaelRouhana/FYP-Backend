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
    List<Community> findByUsers_Id(Long userId);
    
    boolean existsByName(String name);
    
    java.util.Optional<Community> findByName(String name);
    
    java.util.Optional<Community> findByInviteCode(String inviteCode);
    
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE community_users SET role = :role WHERE community_id = :communityId AND user_id = :userId", nativeQuery = true)
    void updateMemberRole(@Param("communityId") Long communityId, @Param("userId") Long userId, @Param("role") String role);
    
    @Query(value = "SELECT COUNT(*) > 0 FROM community_users WHERE community_id = :communityId AND user_id = :userId", nativeQuery = true)
    boolean existsByCommunityIdAndUserId(@Param("communityId") Long communityId, @Param("userId") Long userId);
}
