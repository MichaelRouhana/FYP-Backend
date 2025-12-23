package com.example.FYP.Api.Service;


import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetResolverService {

    private final BetRepository betRepository;
    private final FixtureRepository fixtureRepository;

    @Transactional
    public void resolveBetsForFixture(Long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> ApiRequestException.badRequest("Fixture not found"));

        if (fixture.getGoals() == null) {
            throw ApiRequestException.badRequest("Fixture score not available yet");
        }

        List<Bet> bets = betRepository.findByFixture(fixture);

        for (Bet bet : bets) {
            boolean won = checkIfBetWon(bet, fixture);
            bet.setStatus(won ? BetStatus.WON : BetStatus.LOST);
        }

        betRepository.saveAll(bets);
    }

    private boolean checkIfBetWon(Bet bet, Fixture fixture) {
        Integer homeGoals = fixture.getGoals().get("home");
        Integer awayGoals = fixture.getGoals().get("away");

        switch (bet.getMarketType()) {
            case MATCH_WINNER:
                if (bet.getSelection().equalsIgnoreCase("HOME")) {
                    return homeGoals > awayGoals;
                } else if (bet.getSelection().equalsIgnoreCase("AWAY")) {
                    return awayGoals > homeGoals;
                } else if (bet.getSelection().equalsIgnoreCase("DRAW")) {
                    return homeGoals.equals(awayGoals);
                }
                break;

            case BOTH_TEAMS_TO_SCORE:
                return homeGoals > 0 && awayGoals > 0;

            case GOALS_OVER_UNDER:
                // Expect selection like "OVER 2.5" or "UNDER 1.5"
                String[] parts = bet.getSelection().split(" ");
                if (parts.length == 2) {
                    double threshold = Double.parseDouble(parts[1]);
                    if (parts[0].equalsIgnoreCase("OVER")) {
                        return (homeGoals + awayGoals) > threshold;
                    } else if (parts[0].equalsIgnoreCase("UNDER")) {
                        return (homeGoals + awayGoals) < threshold;
                    }
                }
                break;

            case FIRST_TEAM_TO_SCORE:
                // Expect selection "HOME" or "AWAY" (requires fixture first scorer)
                if (fixture.getFirstTeamToScore() != null) {
                    return bet.getSelection().equalsIgnoreCase(fixture.getFirstTeamToScore());
                }
                break;

            case DOUBLE_CHANCE:
                // Expect selection like "HOME_OR_DRAW", "AWAY_OR_DRAW", "HOME_OR_AWAY"
                switch (bet.getSelection().toUpperCase()) {
                    case "HOME_OR_DRAW":
                        return homeGoals >= awayGoals;
                    case "AWAY_OR_DRAW":
                        return awayGoals >= homeGoals;
                    case "HOME_OR_AWAY":
                        return homeGoals.equals(awayGoals) == false;
                }
                break;

            case SCORE_PREDICTION:
                // Expect selection like "2-1"
                String[] scoreParts = bet.getSelection().split("-");
                if (scoreParts.length == 2) {
                    int predictedHome = Integer.parseInt(scoreParts[0]);
                    int predictedAway = Integer.parseInt(scoreParts[1]);
                    return homeGoals == predictedHome && awayGoals == predictedAway;
                }
                break;
        }

        return false;
    }
}
