package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FixtureRepository extends JpaRepository<Fixture, Long>, JpaSpecificationExecutor<Fixture> {
    
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
    
    @Query(value = "SELECT * FROM fixtures f WHERE f.raw_json LIKE '%\"short\":\"NS\"%'",
            nativeQuery = true)
    List<Fixture> findAllNotStartedFixtures();
    
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

    @Query(value = "SELECT DISTINCT f.* FROM fixtures f " +
            "INNER JOIN bet b ON b.fixture_id = f.id " +
            "WHERE b.status = 'PENDING' " +
            "AND (f.raw_json LIKE '%\"short\":\"FT\"%' OR " +
            "     f.raw_json LIKE '%\"short\":\"AET\"%' OR " +
            "     f.raw_json LIKE '%\"short\":\"PEN\"%')",
            nativeQuery = true)
    List<Fixture> findFinishedFixturesWithPendingBets();

    @Query(value = "SELECT * FROM fixtures f " +
            "WHERE (CAST(JSON_EXTRACT(f.raw_json, '$.teams.home.id') AS UNSIGNED) = :teamId " +
            "   OR CAST(JSON_EXTRACT(f.raw_json, '$.teams.away.id') AS UNSIGNED) = :teamId) " +
            "AND (f.raw_json LIKE '%\"short\":\"FT\"%' OR " +
            "     f.raw_json LIKE '%\"short\":\"AET\"%' OR " +
            "     f.raw_json LIKE '%\"short\":\"PEN\"%') " +
            "ORDER BY JSON_UNQUOTE(JSON_EXTRACT(f.raw_json, '$.fixture.date')) DESC " +
            "LIMIT 1",
            nativeQuery = true)
    List<Fixture> findLastFinishedMatchByTeamId(@org.springframework.data.repository.query.Param("teamId") Long teamId);
}
