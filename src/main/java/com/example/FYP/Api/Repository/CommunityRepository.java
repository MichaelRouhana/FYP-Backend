package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    // Find all communities where the user is a member
    List<Community> findByUsers_Id(Long userId);
    
    // Check if a community with the given name already exists
    boolean existsByName(String name);
    
    // Find a community by name (optional, for validation)
    java.util.Optional<Community> findByName(String name);
}
