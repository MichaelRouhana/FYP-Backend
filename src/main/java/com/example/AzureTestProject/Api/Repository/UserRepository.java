package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.User;
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


    @Query("SELECT u FROM User u JOIN u.projects p WHERE p.id = :projectId")
    List<User> findAllUsersByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT u FROM User u JOIN u.organizations o WHERE o.id = :organizationId")
    List<User> findAllUsersByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT u FROM User u JOIN u.organizations o WHERE u.email = :email AND o.id = :organizationId")
    Optional<User> findByEmailAndOrganizationId(@Param("email") String email, @Param("organizationId") Long organizationId);


    @Query("SELECT u FROM User u JOIN u.projects o WHERE u.email = :email AND o.id = :projectId")
    Optional<User> findByEmailAndProjectId(@Param("email") String email, @Param("projectId") Long projectId);
}
