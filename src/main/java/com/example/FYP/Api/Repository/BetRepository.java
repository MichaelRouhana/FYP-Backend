package com.example.FYP.Api.Repository;

import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MarketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long>, JpaSpecificationExecutor<Bet> {

    List<Bet> findByStatus(BetStatus status);

    List<Bet> findByFixture(Fixture fixture);

    List<Bet> findByFixtureAndStatus(Fixture fixture, BetStatus status);

    List<Bet> findByUserId(Long userId);

    List<Bet> findByMarketType(MarketType marketType);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, BetStatus status);

    long countByStatus(BetStatus status);

    long countByCreatedDateAfter(java.time.LocalDateTime date);
    long countByStatusAndCreatedDateAfter(BetStatus status, java.time.LocalDateTime date);

    List<Bet> findByTicketId(String ticketId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.user.id = :userId AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.ticketId IS NOT NULL"
    )
    long countDistinctTickets();

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.status = :status AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByStatus(BetStatus status);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.user.id = :userId AND b.status = :status AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByUserIdAndStatus(Long userId, BetStatus status);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.createdDate > :date AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByCreatedDateAfter(java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.status = :status AND b.createdDate > :date AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByStatusAndCreatedDateAfter(BetStatus status, java.time.LocalDateTime date);
}
