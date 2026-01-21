package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findByUsernameContainingIgnoreCase(String search, Pageable pageable);

    Page<User> findAll(Pageable pageable);

    Page<User> findAllByOrderByPointsDesc(Pageable pageable);

    @Query(value = """
        SELECT u.*
        FROM users u
        LEFT JOIN (
            SELECT user_id, COUNT(*) as win_count
            FROM bet
            WHERE status = :status
            GROUP BY user_id
        ) wins ON u.id = wins.user_id
        ORDER BY COALESCE(wins.win_count, 0) DESC, u.id ASC
    """, 
    countQuery = "SELECT COUNT(*) FROM users",
    nativeQuery = true)
    Page<User> findTopBetters(@Param("status") String status, Pageable pageable);

    @Query("""
    SELECT u
    FROM User u
    LEFT JOIN u.bets b
    GROUP BY u
    ORDER BY COUNT(b) DESC
""")
    List<User> findTop10UsersByBetCount();

    List<User> findTop10ByOrderByPointsDesc();

    @Query("SELECT u FROM User u JOIN u.communities o WHERE u.email = :email AND o.id = :communityId")
    Optional<User> findByEmailAndCommunityId(@Param("email") String email, @Param("communityId") Long communityId);

    @Query("SELECT u FROM User u WHERE u.createdDate >= :startDate ORDER BY u.createdDate ASC")
    List<User> findByCreatedDateAfter(@Param("startDate") java.time.LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdDate < :startDate")
    long countByCreatedDateBefore(@Param("startDate") java.time.LocalDateTime startDate);
    
    @Query("""
        SELECT DISTINCT u 
        FROM User u 
        LEFT JOIN u.bets b 
        WHERE u.createdDate >= :startDate 
        AND (
            EXISTS (
                SELECT 1 FROM Bet bet 
                WHERE bet.user.id = u.id 
                AND bet.createdDate >= :activityThreshold
            )
            OR u.lastModifiedDate >= :activityThreshold
        )
        ORDER BY u.createdDate ASC
    """)
    List<User> findActiveUsersByCreatedDateAfter(
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("activityThreshold") java.time.LocalDateTime activityThreshold
    );
    
    @Query("""
        SELECT COUNT(DISTINCT u) 
        FROM User u 
        LEFT JOIN u.bets b 
        WHERE u.createdDate < :startDate 
        AND (
            EXISTS (
                SELECT 1 FROM Bet bet 
                WHERE bet.user.id = u.id 
                AND bet.createdDate >= :activityThreshold
            )
            OR u.lastModifiedDate >= :activityThreshold
        )
    """)
    long countActiveUsersByCreatedDateBefore(
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("activityThreshold") java.time.LocalDateTime activityThreshold
    );

}
