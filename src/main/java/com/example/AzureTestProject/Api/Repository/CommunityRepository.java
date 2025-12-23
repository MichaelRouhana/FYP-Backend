package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
}
