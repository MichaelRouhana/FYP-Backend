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
}
