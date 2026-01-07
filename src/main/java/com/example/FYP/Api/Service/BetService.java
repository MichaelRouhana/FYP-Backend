package com.example.FYP.Api.Service;


import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Model.Filter.BetFilterDTO;
import com.example.FYP.Api.Model.Request.BetRequestDTO;
import com.example.FYP.Api.Model.Request.BetLegDTO;
import com.example.FYP.Api.Model.Response.BetResponseDTO;
import com.example.FYP.Api.Model.Response.BetLegResponseDTO;
import com.example.FYP.Api.Model.View.BetViewAllDTO;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.example.FYP.Api.Repository.UserRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Specification.GenericSpecification;
import com.example.FYP.Api.Util.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;
    private final ModelMapper modelMapper;
    private final SecurityContext securityContext;
    private final FixtureRepository fixtureRepository;
    private final UserRepository userRepository;

    @Transactional
    public BetResponseDTO create(BetRequestDTO betDTO) {
        // Validate stake
        if (betDTO.getStake() == null || betDTO.getStake() <= 0) {
            throw ApiRequestException.badRequest("Stake must be greater than 0");
        }

        // Validate legs
        if (betDTO.getLegs() == null || betDTO.getLegs().isEmpty()) {
            throw ApiRequestException.badRequest("At least one leg is required");
        }

        // Get current user and reload from database to ensure we have latest balance
        User currentUser = securityContext.getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> ApiRequestException.badRequest("User not found"));
        
        // Check if user has sufficient balance
        Long stakeLong = betDTO.getStake().longValue();
        if (user.getPoints() == null || user.getPoints() < stakeLong) {
            throw ApiRequestException.badRequest("Insufficient balance. You have " + 
                (user.getPoints() != null ? user.getPoints() : 0) + " PTS but need " + stakeLong + " PTS");
        }

        // Validate all fixtures and check betting is allowed
        List<Bet> bets = new ArrayList<>();
        BigDecimal totalOdds = BigDecimal.ONE;
        
        for (var leg : betDTO.getLegs()) {
            Fixture fixture = fixtureRepository.findById(leg.getFixtureId())
                    .orElseThrow(() -> ApiRequestException.badRequest("Fixture not found: " + leg.getFixtureId()));

            if (!fixture.getMatchSettings().getAllowBetting().equals(Boolean.TRUE)) {
                throw ApiRequestException.badRequest("Cannot bet on fixture: " + leg.getFixtureId());
            }

            // Create bet entity for this leg
            Bet bet = Bet.builder()
                    .marketType(leg.getMarketType())
                    .fixture(fixture)
                    .selection(leg.getSelection())
                    .stake(betDTO.getStake()) // Same stake for all legs in accumulator
                    .odd(leg.getOdd())
                    .status(BetStatus.PENDING)
                    .user(user)
                    .build();

            bets.add(bet);
            
            // Calculate total odds (multiply all leg odds)
            if (leg.getOdd() != null) {
                totalOdds = totalOdds.multiply(leg.getOdd());
            }
        }

        // Deduct stake from user balance (only once for the accumulator)
        user.setPoints(user.getPoints() - stakeLong);
        userRepository.save(user);

        // Save all bet legs
        betRepository.saveAll(bets);
        
        // Calculate potential winnings
        BigDecimal potentialWinnings = BigDecimal.valueOf(betDTO.getStake()).multiply(totalOdds);
        
        log.info("Accumulator bet created: {} legs, User={}, Stake={}, Total Odds={}, Potential Winnings={}, Balance after={}", 
            bets.size(), user.getEmail(), betDTO.getStake(), totalOdds, potentialWinnings, user.getPoints());

        // Build response with all legs
        List<BetLegResponseDTO> legResponses = new ArrayList<>();
        for (Bet bet : bets) {
            BetLegResponseDTO legResponse = new BetLegResponseDTO();
            legResponse.setId(bet.getId());
            legResponse.setFixtureId(bet.getFixture().getId());
            legResponse.setMarketType(bet.getMarketType());
            legResponse.setSelection(bet.getSelection());
            legResponse.setOdd(bet.getOdd());
            legResponse.setStatus(bet.getStatus());
            legResponses.add(legResponse);
        }

        // Use the first bet's ID as the accumulator bet ID
        BetResponseDTO response = new BetResponseDTO();
        response.setId(bets.get(0).getId());
        response.setStake(betDTO.getStake());
        response.setTotalOdds(totalOdds);
        response.setPotentialWinnings(potentialWinnings);
        response.setStatus(BetStatus.PENDING); // Overall status is PENDING until all legs are resolved
        response.setLegs(legResponses);
        
        // Map fixture details from the first leg's fixture (for main display)
        if (!bets.isEmpty() && bets.get(0).getFixture() != null) {
            Fixture firstFixture = bets.get(0).getFixture();
            response.setHomeTeam(firstFixture.getHomeTeamName());
            response.setAwayTeam(firstFixture.getAwayTeamName());
            response.setHomeTeamLogo(firstFixture.getHomeTeamLogo());
            response.setAwayTeamLogo(firstFixture.getAwayTeamLogo());
            response.setMatchStatus(firstFixture.getStatusShort());
            
            // Map scores from goals
            Map<String, Integer> goals = firstFixture.getGoals();
            if (goals != null) {
                response.setHomeScore(goals.get("home"));
                response.setAwayScore(goals.get("away"));
            }
            
            // Map match date from fixture date
            String dateStr = firstFixture.getFixtureDate();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    // Parse ISO 8601 format: "2024-01-15T20:00:00+00:00" or "2024-01-15T20:00:00Z"
                    // Remove timezone info and parse as LocalDateTime
                    String normalizedDate = dateStr.replace("Z", "");
                    if (normalizedDate.contains("+")) {
                        normalizedDate = normalizedDate.substring(0, normalizedDate.indexOf('+'));
                    } else if (normalizedDate.contains("-") && normalizedDate.lastIndexOf('-') > 10) {
                        // Check if there's a timezone offset (last '-' after the date part)
                        int lastDash = normalizedDate.lastIndexOf('-');
                        if (lastDash > 10) {
                            normalizedDate = normalizedDate.substring(0, lastDash - 3); // Remove "-00:00"
                        }
                    }
                    response.setMatchDate(LocalDateTime.parse(normalizedDate));
                } catch (Exception e) {
                    log.warn("Failed to parse match date for fixture {}: {}", firstFixture.getId(), dateStr, e);
                }
            }
        }
        
        return response;
    }

    public PagedResponse<BetViewAllDTO> getAll(Pageable pageable, BetFilterDTO filter) {
        // Get current user to filter bets
        User currentUser = securityContext.getCurrentUser();

        Specification<Bet> specification = Specification.where(
                GenericSpecification.<Bet>filterByFields(filter)
        ).and((root, query, criteriaBuilder) -> {
            // Filter by current user - only return bets belonging to the authenticated user
            return criteriaBuilder.equal(root.get("user").get("id"), currentUser.getId());
        });

        Page<Bet> purchaseOrderPage = betRepository.findAll(specification, pageable);

        Page<BetViewAllDTO> accountResponsePage = purchaseOrderPage.map(bet -> {
            BetViewAllDTO dto = modelMapper.map(bet, BetViewAllDTO.class);
            // Manually set fixtureId since ModelMapper might not map nested fields automatically
            if (bet.getFixture() != null) {
                Fixture fixture = bet.getFixture();
                dto.setFixtureId(fixture.getId());
                
                // Map fixture details
                dto.setHomeTeam(fixture.getHomeTeamName());
                dto.setAwayTeam(fixture.getAwayTeamName());
                dto.setHomeTeamLogo(fixture.getHomeTeamLogo());
                dto.setAwayTeamLogo(fixture.getAwayTeamLogo());
                dto.setMatchStatus(fixture.getStatusShort());
                
                // Map scores from goals
                Map<String, Integer> goals = fixture.getGoals();
                if (goals != null) {
                    dto.setHomeScore(goals.get("home"));
                    dto.setAwayScore(goals.get("away"));
                }
                
                // Map match date from fixture date
                String dateStr = fixture.getFixtureDate();
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        // Parse ISO 8601 format: "2024-01-15T20:00:00+00:00" or "2024-01-15T20:00:00Z"
                        // Remove timezone info and parse as LocalDateTime
                        String normalizedDate = dateStr.replace("Z", "");
                        if (normalizedDate.contains("+")) {
                            normalizedDate = normalizedDate.substring(0, normalizedDate.indexOf('+'));
                        } else if (normalizedDate.contains("-") && normalizedDate.lastIndexOf('-') > 10) {
                            // Check if there's a timezone offset (last '-' after the date part)
                            int lastDash = normalizedDate.lastIndexOf('-');
                            if (lastDash > 10) {
                                normalizedDate = normalizedDate.substring(0, lastDash - 3); // Remove "-00:00"
                            }
                        }
                        dto.setMatchDate(LocalDateTime.parse(normalizedDate));
                    } catch (Exception e) {
                        log.warn("Failed to parse match date for fixture {}: {}", fixture.getId(), dateStr, e);
                    }
                }
            }
            // Set createdDate for grouping accumulator bets
            if (bet.getCreatedDate() != null) {
                dto.setCreatedDate(bet.getCreatedDate());
            }
            return dto;
        });

        return PagedResponse.fromPage(accountResponsePage);
    }

    public BetResponseDTO getBetById(Long id) {
        // Get current user to ensure they can only access their own bets
        User currentUser = securityContext.getCurrentUser();
        
        // Find the bet by ID
        Bet bet = betRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException(HttpStatus.NOT_FOUND, "Bet not found with id: " + id));
        
        // Verify the bet belongs to the current user
        if (!bet.getUser().getId().equals(currentUser.getId())) {
            throw ApiRequestException.badRequest("You do not have access to this bet");
        }
        
        // For accumulator bets, we need to find all legs with the same stake and created within 2 seconds
        // For simplicity, we'll query for bets with the same user, stake, and created date within a 2-second window
        List<Bet> allBets = betRepository.findByUserId(currentUser.getId());
        List<Bet> accumulatorBets = allBets.stream()
                .filter(b -> b.getStake().equals(bet.getStake()) &&
                        b.getCreatedDate() != null &&
                        bet.getCreatedDate() != null &&
                        Math.abs(java.time.Duration.between(b.getCreatedDate(), bet.getCreatedDate()).getSeconds()) <= 2)
                .toList();
        
        // Use accumulator bets if found, otherwise use just this bet
        List<Bet> betsToReturn = accumulatorBets.size() > 1 ? accumulatorBets : List.of(bet);
        
        // Build response similar to create() method
        BigDecimal totalOdds = BigDecimal.ONE;
        for (Bet b : betsToReturn) {
            if (b.getOdd() != null) {
                totalOdds = totalOdds.multiply(b.getOdd());
            }
        }
        
        BigDecimal potentialWinnings = BigDecimal.valueOf(bet.getStake()).multiply(totalOdds);
        
        // Build leg responses
        List<BetLegResponseDTO> legResponses = new ArrayList<>();
        for (Bet b : betsToReturn) {
            BetLegResponseDTO legResponse = new BetLegResponseDTO();
            legResponse.setId(b.getId());
            legResponse.setFixtureId(b.getFixture().getId());
            legResponse.setMarketType(b.getMarketType());
            legResponse.setSelection(b.getSelection());
            legResponse.setOdd(b.getOdd());
            legResponse.setStatus(b.getStatus());
            legResponses.add(legResponse);
        }
        
        // Build response
        BetResponseDTO response = new BetResponseDTO();
        response.setId(bet.getId());
        response.setStake(bet.getStake());
        response.setTotalOdds(totalOdds);
        response.setPotentialWinnings(potentialWinnings);
        
        // Determine overall status: if all legs are won, status is WON; if any is lost, status is LOST; otherwise PENDING
        boolean allWon = betsToReturn.stream().allMatch(b -> b.getStatus() == BetStatus.WON);
        boolean anyLost = betsToReturn.stream().anyMatch(b -> b.getStatus() == BetStatus.LOST);
        if (allWon) {
            response.setStatus(BetStatus.WON);
        } else if (anyLost) {
            response.setStatus(BetStatus.LOST);
        } else {
            response.setStatus(BetStatus.PENDING);
        }
        
        response.setLegs(legResponses);
        
        // Map fixture details from the first leg's fixture (for main display)
        if (!betsToReturn.isEmpty() && betsToReturn.get(0).getFixture() != null) {
            Fixture firstFixture = betsToReturn.get(0).getFixture();
            response.setHomeTeam(firstFixture.getHomeTeamName());
            response.setAwayTeam(firstFixture.getAwayTeamName());
            response.setHomeTeamLogo(firstFixture.getHomeTeamLogo());
            response.setAwayTeamLogo(firstFixture.getAwayTeamLogo());
            response.setMatchStatus(firstFixture.getStatusShort());
            
            // Map scores from goals
            Map<String, Integer> goals = firstFixture.getGoals();
            if (goals != null) {
                response.setHomeScore(goals.get("home"));
                response.setAwayScore(goals.get("away"));
            }
            
            // Map match date from fixture date
            String dateStr = firstFixture.getFixtureDate();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    // Parse ISO 8601 format: "2024-01-15T20:00:00+00:00" or "2024-01-15T20:00:00Z"
                    // Remove timezone info and parse as LocalDateTime
                    String normalizedDate = dateStr.replace("Z", "");
                    if (normalizedDate.contains("+")) {
                        normalizedDate = normalizedDate.substring(0, normalizedDate.indexOf('+'));
                    } else if (normalizedDate.contains("-") && normalizedDate.lastIndexOf('-') > 10) {
                        // Check if there's a timezone offset (last '-' after the date part)
                        int lastDash = normalizedDate.lastIndexOf('-');
                        if (lastDash > 10) {
                            normalizedDate = normalizedDate.substring(0, lastDash - 3); // Remove "-00:00"
                        }
                    }
                    response.setMatchDate(LocalDateTime.parse(normalizedDate));
                } catch (Exception e) {
                    log.warn("Failed to parse match date for fixture {}: {}", firstFixture.getId(), dateStr, e);
                }
            }
        }
        
        return response;
    }
}
