package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.User;
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
