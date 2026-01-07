package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FixtureRepository extends JpaRepository<Fixture, Long>, JpaSpecificationExecutor<Fixture> {
    
    /**
     * Finds all fixtures that are currently marked as "live" in their rawJson.
     * Live statuses include: 1H, 2H, HT, ET, P, LIVE, BT, INT
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
     * Finds fixtures that are "Not Started" (NS).
     * These might need to transition to live or finished.
     */
    @Query(value = "SELECT * FROM fixtures f WHERE f.raw_json LIKE '%\"short\":\"NS\"%'",
            nativeQuery = true)
    List<Fixture> findAllNotStartedFixtures();
    
    /**
     * Finds all fixtures that are NOT finished yet.
     * This includes NS, live statuses, and any other non-finished status.
     * Excludes: FT, AET, PEN, PST, CANC, ABD, AWD, WO
     */
    @Query(value = "SELECT * FROM fixtures f WHERE " +
            "f.raw_json NOT LIKE '%\"short\":\"FT\"%' AND " +
            "f.raw_json NOT LIKE '%\"short\":\"AET\"%' AND " +
            "f.raw_json NOT LIKE '%\"short\":\"PEN\"%' AND " +
            "f.raw_json NOT LIKE '%\"short\":\"PST\"%' AND " +
            "f.raw_json NOT LIKE '%\"short\":\"CANC\"%' AND " +
            "f.raw_json NOT LIKE '%\"short\":\"ABD\"%' AND " +
            "f.raw_json NOT LIKE '%\"short\":\"AWD\"%' AND " +
            "f.raw_json NOT LIKE '%\"short\":\"WO\"%'",
            nativeQuery = true)
    List<Fixture> findAllUnfinishedFixtures();

    /**
     * Finds fixtures that are finished (FT, AET, PEN) AND have pending bets.
     * This ensures we retry resolution for any missed bets.
     */
    @Query(value = "SELECT DISTINCT f.* FROM fixtures f " +
            "INNER JOIN bet b ON b.fixture_id = f.id " +
            "WHERE b.status = 'PENDING' " +
            "AND (f.raw_json LIKE '%\"short\":\"FT\"%' OR " +
            "     f.raw_json LIKE '%\"short\":\"AET\"%' OR " +
            "     f.raw_json LIKE '%\"short\":\"PEN\"%')",
            nativeQuery = true)
    List<Fixture> findFinishedFixturesWithPendingBets();
}
