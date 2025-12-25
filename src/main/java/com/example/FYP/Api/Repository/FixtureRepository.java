package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FixtureRepository extends JpaRepository<Fixture, Long>, JpaSpecificationExecutor<Fixture> {
    
    /**
     * Finds all fixtures that are currently marked as "live" in their rawJson.
     * Live statuses include: 1H, 2H, HT, ET, P, LIVE, BT
     * This uses a native query with JSON extraction for PostgreSQL.
     * For H2/other databases, we'll use a simpler approach with LIKE.
     */
    @Query(value = "SELECT * FROM fixtures f WHERE " +
            "f.raw_json LIKE '%\"short\":\"1H\"%' OR " +
            "f.raw_json LIKE '%\"short\":\"2H\"%' OR " +
            "f.raw_json LIKE '%\"short\":\"HT\"%' OR " +
            "f.raw_json LIKE '%\"short\":\"ET\"%' OR " +
            "f.raw_json LIKE '%\"short\":\"P\"%' OR " +
            "f.raw_json LIKE '%\"short\":\"BT\"%' OR " +
            "f.raw_json LIKE '%\"short\":\"LIVE\"%' OR " +
            "f.raw_json LIKE '%\"short\":\"INT\"%'",
            nativeQuery = true)
    List<Fixture> findAllLiveFixtures();
    
    /**
     * Finds fixtures by status - useful for finding matches that need resolution.
     * Matches that are "Not Started" (NS) and scheduled for today might become live.
     */
    @Query(value = "SELECT * FROM fixtures f WHERE f.raw_json LIKE '%\"short\":\"NS\"%'",
            nativeQuery = true)
    List<Fixture> findAllNotStartedFixtures();
}
