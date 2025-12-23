package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Invitation;
import com.example.FYP.Api.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    boolean existsByUuid(String uuid);

    Optional<Invitation> findByUuid(String uuid);

    List<Invitation> findByInvitee(User invitee);

    @Query("SELECT COUNT(i) > 0 FROM Invitation i WHERE i.invitee.email = :email")
    boolean existsByInviteeEmail(@Param("email") String email);
}
