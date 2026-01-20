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

    // Count methods for profile statistics
    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, BetStatus status);

    // Count methods for dashboard statistics
    long countByStatus(BetStatus status);

    // Count methods with date filtering for time range support
    long countByCreatedDateAfter(java.time.LocalDateTime date);
    long countByStatusAndCreatedDateAfter(BetStatus status, java.time.LocalDateTime date);

    // Find all legs of a ticket (accumulator)
    List<Bet> findByTicketId(String ticketId);

    // Count distinct tickets (not legs) for a user
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.user.id = :userId AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByUserId(Long userId);

    // Count distinct tickets (not legs) for all bets
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.ticketId IS NOT NULL"
    )
    long countDistinctTickets();

    // Count distinct tickets (not legs) with status filter
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.status = :status AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByStatus(BetStatus status);

    // Count distinct tickets (not legs) for a user with status filter
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.user.id = :userId AND b.status = :status AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByUserIdAndStatus(Long userId, BetStatus status);

    // Count distinct tickets (not legs) created after a date
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.createdDate > :date AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByCreatedDateAfter(java.time.LocalDateTime date);

    // Count distinct tickets (not legs) with status and date filter
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT b.ticketId) FROM Bet b WHERE b.status = :status AND b.createdDate > :date AND b.ticketId IS NOT NULL"
    )
    long countDistinctTicketsByStatusAndCreatedDateAfter(BetStatus status, java.time.LocalDateTime date);
}
