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
        resolveBetsForFixture(fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> ApiRequestException.badRequest("Fixture not found")));
    }

    @Transactional
    public void resolveBetsForFixture(Fixture fixture) {
        if (fixture.getGoals() == null) {
            log.warn("Fixture {} has no goals data, skipping bet resolution", fixture.getId());
            return;
        }

        // Get ALL bets for this fixture (including those that might have been missed)
        List<Bet> bets = betRepository.findByFixture(fixture);
        
        if (bets.isEmpty()) {
            log.debug("No bets found for fixture {}", fixture.getId());
            return;
        }

        log.info("Resolving {} bets for fixture {}", bets.size(), fixture.getId());
        int resolvedCount = 0;
        int errorCount = 0;

        // Iterate through ALL bets and resolve each one individually with error handling
        for (Bet bet : bets) {
            // Only resolve pending bets
            if (bet.getStatus() != BetStatus.PENDING) {
                continue;
            }

            try {
                BetStatus resolvedStatus = checkIfBetWon(bet, fixture);
                if (resolvedStatus != null) {
                    bet.setStatus(resolvedStatus);
                    betRepository.save(bet);
                    resolvedCount++;
                    log.debug("Bet {} resolved as {}", bet.getId(), bet.getStatus());
                } else {
                    // Unknown market type - void the bet
                    log.warn("Unknown market type {} for bet {} (fixture {}). Voiding bet.", 
                            bet.getMarketType(), bet.getId(), fixture.getId());
                    bet.setStatus(BetStatus.VOID);
                    betRepository.save(bet);
                    resolvedCount++;
                }
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to resolve bet {} (fixture {}, market {}, selection {}): {}", 
                        bet.getId(), fixture.getId(), bet.getMarketType(), bet.getSelection(), 
                        e.getMessage(), e);
                // Continue to next bet - don't let one failure stop the batch
            }
        }

        log.info("Fixture {} resolution complete: {} resolved, {} errors", 
                fixture.getId(), resolvedCount, errorCount);
    }

    private BetStatus checkIfBetWon(Bet bet, Fixture fixture) {
        Integer homeGoals = fixture.getGoals().get("home");
        Integer awayGoals = fixture.getGoals().get("away");

        switch (bet.getMarketType()) {
            case MATCH_WINNER:
                if (bet.getSelection().equalsIgnoreCase("HOME")) {
                    return homeGoals > awayGoals ? BetStatus.WON : BetStatus.LOST;
                } else if (bet.getSelection().equalsIgnoreCase("AWAY")) {
                    return awayGoals > homeGoals ? BetStatus.WON : BetStatus.LOST;
                } else if (bet.getSelection().equalsIgnoreCase("DRAW")) {
                    return homeGoals.equals(awayGoals) ? BetStatus.WON : BetStatus.LOST;
                }
                break;

            case BOTH_TEAMS_TO_SCORE:
                boolean bothScored = homeGoals > 0 && awayGoals > 0;
                if (bet.getSelection().equalsIgnoreCase("Yes")) {
                    return bothScored ? BetStatus.WON : BetStatus.LOST;
                } else if (bet.getSelection().equalsIgnoreCase("No")) {
                    return !bothScored ? BetStatus.WON : BetStatus.LOST;
                }
                break;

            case GOALS_OVER_UNDER:
                // Expect selection like "OVER 2.5" or "UNDER 1.5"
                String[] parts = bet.getSelection().split(" ");
                if (parts.length == 2) {
                    double threshold = Double.parseDouble(parts[1]);
                    double totalGoals = homeGoals + awayGoals;
                    if (parts[0].equalsIgnoreCase("OVER")) {
                        return totalGoals > threshold ? BetStatus.WON : BetStatus.LOST;
                    } else if (parts[0].equalsIgnoreCase("UNDER")) {
                        return totalGoals < threshold ? BetStatus.WON : BetStatus.LOST;
                    }
                }
                break;

            case FIRST_TEAM_TO_SCORE:
                // Expect selection "HOME" or "AWAY" (requires fixture first scorer)
                if (fixture.getFirstTeamToScore() != null) {
                    return bet.getSelection().equalsIgnoreCase(fixture.getFirstTeamToScore()) 
                            ? BetStatus.WON : BetStatus.LOST;
                }
                // If first scorer data not available, void the bet
                return BetStatus.VOID;

            case DOUBLE_CHANCE:
                // Expect selection like "HOME_OR_DRAW", "AWAY_OR_DRAW", "HOME_OR_AWAY"
                // Or "X1", "X2", "12" format
                String selection = bet.getSelection().toUpperCase();
                if (selection.equals("HOME_OR_DRAW") || selection.equals("X1")) {
                    return homeGoals >= awayGoals ? BetStatus.WON : BetStatus.LOST;
                } else if (selection.equals("AWAY_OR_DRAW") || selection.equals("X2")) {
                    return awayGoals >= homeGoals ? BetStatus.WON : BetStatus.LOST;
                } else if (selection.equals("HOME_OR_AWAY") || selection.equals("12")) {
                    return !homeGoals.equals(awayGoals) ? BetStatus.WON : BetStatus.LOST;
                }
                break;

            case SCORE_PREDICTION:
                // Expect selection like "2-1"
                String[] scoreParts = bet.getSelection().split("-");
                if (scoreParts.length == 2) {
                    int predictedHome = Integer.parseInt(scoreParts[0]);
                    int predictedAway = Integer.parseInt(scoreParts[1]);
                    return (homeGoals == predictedHome && awayGoals == predictedAway) 
                            ? BetStatus.WON : BetStatus.LOST;
                }
                break;

            default:
                // Unknown market type - return null to signal void
                return null;
        }

        // If we reach here, the selection format was invalid
        return null;
    }
}
