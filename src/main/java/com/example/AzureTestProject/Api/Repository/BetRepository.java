package com.example.AzureTestProject.Api.Repository;

import com.example.AzureTestProject.Api.Entity.Bet;
import com.example.AzureTestProject.Api.Entity.BetStatus;
import com.example.AzureTestProject.Api.Entity.Fixture;
import com.example.AzureTestProject.Api.Entity.MarketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long>, JpaSpecificationExecutor<Bet> {

    List<Bet> findByStatus(BetStatus status);

    List<Bet> findByFixture(Fixture fixture);

    List<Bet> findByFixtureAndStatus(Fixture fixture, BetStatus status);

    List<Bet> findByUserId(Long userId);

    List<Bet> findByMarketType(MarketType marketType);
}
