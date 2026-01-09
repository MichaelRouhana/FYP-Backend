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

    // Search users by username (case-insensitive)
    Page<User> findByUsernameContainingIgnoreCase(String search, Pageable pageable);

    // Find all users with pagination
    Page<User> findAll(Pageable pageable);

    // Find users sorted by points (descending)
    Page<User> findAllByOrderByPointsDesc(Pageable pageable);

    // Find top betters (users sorted by number of won bets)
    @Query(value = """
        SELECT u.*
        FROM users u
        LEFT JOIN (
            SELECT user_id, COUNT(*) as win_count
            FROM bets
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

}
