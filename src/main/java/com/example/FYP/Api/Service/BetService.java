package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Exception.ResourceNotFoundException;
import com.example.FYP.Api.Model.Filter.BetFilterDTO;
import com.example.FYP.Api.Model.Request.BetRequestDTO;
import com.example.FYP.Api.Model.Response.BetLegResponseDTO;
import com.example.FYP.Api.Model.Response.BetResponseDTO;
import com.example.FYP.Api.Model.View.BetViewAllDTO;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.example.FYP.Api.Repository.UserRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Specification.GenericSpecification;
import com.example.FYP.Api.Util.PagedResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;
    private final ModelMapper modelMapper;
    private final SecurityContext securityContext;
    private final FixtureRepository fixtureRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // Required for JSON parsing

    @Transactional
    public BetResponseDTO create(BetRequestDTO betDTO) {
        if (betDTO.getStake() == null || betDTO.getStake() <= 0) {
            throw ApiRequestException.badRequest("Stake must be greater than 0");
        }
        if (betDTO.getLegs() == null || betDTO.getLegs().isEmpty()) {
            throw ApiRequestException.badRequest("At least one leg is required");
        }

        User currentUser = securityContext.getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> ApiRequestException.badRequest("User not found"));
        
        long stakeLong = betDTO.getStake().longValue(); 
        if (user.getPoints() == null || user.getPoints() < stakeLong) {
            throw ApiRequestException.badRequest("Insufficient balance.");
        }

        // Generate a unique ticketId to group all legs of this accumulator/ticket
        String ticketId = UUID.randomUUID().toString();
        
        List<Bet> bets = new ArrayList<>();
        BigDecimal totalOdds = BigDecimal.ONE;
        
        // CRITICAL: For accumulators, all legs share the same ticketId
        // The stake is set on each leg (as reference) but only deducted once from user balance
        for (var leg : betDTO.getLegs()) {
            Fixture fixture = fixtureRepository.findById(leg.getFixtureId())
                    .orElseThrow(() -> ApiRequestException.badRequest("Fixture not found"));

            if (!Boolean.TRUE.equals(fixture.getMatchSettings().getAllowBetting())) {
                throw ApiRequestException.badRequest("Cannot bet on fixture: " + leg.getFixtureId());
            }

            Bet bet = Bet.builder()
                    .marketType(leg.getMarketType())
                    .fixture(fixture)
                    .selection(leg.getSelection())
                    .stake(betDTO.getStake()) // Same stake on each leg for accumulator
                    .odd(leg.getOdd())
                    .status(BetStatus.PENDING)
                    .user(user)
                    .ticketId(ticketId) // CRITICAL: Group all legs with same ticketId
                    .build();

            bets.add(bet);
            if (leg.getOdd() != null) {
                totalOdds = totalOdds.multiply(leg.getOdd());
            }
        }

        user.setPoints(user.getPoints() - stakeLong);
        userRepository.save(user);
        betRepository.saveAll(bets);
        
        BigDecimal potentialWinnings = BigDecimal.valueOf(betDTO.getStake()).multiply(totalOdds);
        
        // Build response manually to ensure structure
        List<BetLegResponseDTO> legResponses = new ArrayList<>();
        for (Bet bet : bets) {
            BetLegResponseDTO leg = new BetLegResponseDTO();
            leg.setId(bet.getId());
            leg.setFixtureId(bet.getFixture().getId());
            leg.setMarketType(bet.getMarketType());
            leg.setSelection(bet.getSelection());
            leg.setOdd(bet.getOdd());
            leg.setStatus(bet.getStatus());
            legResponses.add(leg);
        }

        BetResponseDTO response = new BetResponseDTO();
        response.setId(bets.get(0).getId());
        response.setStake(betDTO.getStake());
        response.setTotalOdds(totalOdds);
        response.setPotentialWinnings(potentialWinnings);
        response.setStatus(BetStatus.PENDING);
        response.setLegs(legResponses);
        
        return response;
    }

    public PagedResponse<BetViewAllDTO> getAll(Pageable pageable, BetFilterDTO filter) {
        User currentUser = securityContext.getCurrentUser();

        Specification<Bet> specification = Specification.where(
                GenericSpecification.<Bet>filterByFields(filter)
        ).and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user").get("id"), currentUser.getId())
        );

        Page<Bet> betPage = betRepository.findAll(specification, pageable);

        Page<BetViewAllDTO> responsePage = betPage.map(bet -> {
            BetViewAllDTO dto = modelMapper.map(bet, BetViewAllDTO.class);
            
            if (bet.getFixture() != null) {
                Fixture f = bet.getFixture();
                dto.setFixtureId(f.getId());
                // Parse JSON string safely
                enrichDtoWithFixtureData(dto, f.getRawJson());
            }
            if (bet.getCreatedDate() != null) {
                dto.setCreatedDate(bet.getCreatedDate());
            }
            return dto;
        });

        return PagedResponse.fromPage(responsePage);
    }

    public BetResponseDTO getBetById(Long id) {
        Bet originalBet = betRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found with id: " + id));

        // Determine if this is part of an accumulator ticket
        List<Bet> ticketLegs;
        if (originalBet.getTicketId() != null && !originalBet.getTicketId().isEmpty()) {
            // This is part of an accumulator - fetch ALL legs with the same ticketId
            ticketLegs = betRepository.findByTicketId(originalBet.getTicketId());
            log.info("Found {} legs for ticket {}", ticketLegs.size(), originalBet.getTicketId());
        } else {
            // Old single-leg bet (backward compatibility) - use just this bet
            ticketLegs = List.of(originalBet);
            log.debug("Bet {} has no ticketId, treating as single-leg bet", id);
        }

        // 1. Manually Build the Response
        BetResponseDTO response = new BetResponseDTO();
        response.setId(originalBet.getId()); // Use the first bet's ID as the ticket ID
        
        // Use the stake from the first leg (all legs should have the same stake for accumulators)
        Double stake = ticketLegs.get(0).getStake();
        response.setStake(stake);
        
        // 2. Calculate Total Odds & Potential Winnings (for accumulator, multiply all leg odds)
        BigDecimal totalOdds = BigDecimal.ONE;
        for (Bet legBet : ticketLegs) {
            if (legBet.getOdd() != null) {
                totalOdds = totalOdds.multiply(legBet.getOdd());
            }
        }
        response.setTotalOdds(totalOdds);
        
        // Calculate potential winnings: stake * totalOdds (for accumulator)
        if (stake != null) {
            response.setPotentialWinnings(totalOdds.multiply(BigDecimal.valueOf(stake)));
        } else {
            response.setPotentialWinnings(BigDecimal.ZERO);
        }

        // Determine overall status: WON if all legs won, LOST if any leg lost, PENDING otherwise
        boolean allWon = ticketLegs.stream().allMatch(b -> b.getStatus() == BetStatus.WON);
        boolean anyLost = ticketLegs.stream().anyMatch(b -> b.getStatus() == BetStatus.LOST);
        boolean anyVoid = ticketLegs.stream().anyMatch(b -> b.getStatus() == BetStatus.VOID);
        
        if (anyVoid) {
            response.setStatus(BetStatus.VOID);
        } else if (allWon) {
            response.setStatus(BetStatus.WON);
        } else if (anyLost) {
            response.setStatus(BetStatus.LOST);
        } else {
            response.setStatus(BetStatus.PENDING);
        }

        // 3. Build ALL legs response (CRITICAL for frontend to render all legs)
        List<BetLegResponseDTO> legResponses = new ArrayList<>();
        for (Bet legBet : ticketLegs) {
            BetLegResponseDTO leg = new BetLegResponseDTO();
            leg.setId(legBet.getId());
            if (legBet.getFixture() != null) {
                leg.setFixtureId(legBet.getFixture().getId());
            }
            leg.setMarketType(legBet.getMarketType());
            leg.setSelection(legBet.getSelection());
            leg.setOdd(legBet.getOdd());
            leg.setStatus(legBet.getStatus());
            legResponses.add(leg);
        }
        
        response.setLegs(legResponses); // <--- Frontend needs ALL legs of the accumulator

        // 4. Enrich with Fixture Data from the FIRST leg (for main display)
        if (!ticketLegs.isEmpty() && ticketLegs.get(0).getFixture() != null) {
            Fixture firstFixture = ticketLegs.get(0).getFixture();
            response.setFixtureId(firstFixture.getId());
            enrichDtoWithFixtureData(response, firstFixture.getRawJson());
        }
        
        return response;
    }

    /**
     * Helper to parse the rawJson String and populate DTO fields
     */
    private void enrichDtoWithFixtureData(Object dto, String rawJson) {
        if (rawJson == null || rawJson.isEmpty()) return;

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode teams = root.path("teams");
            JsonNode goals = root.path("goals");
            JsonNode fixtureNode = root.path("fixture");

            String homeName = teams.path("home").path("name").asText("");
            String homeLogo = teams.path("home").path("logo").asText("");
            String awayName = teams.path("away").path("name").asText("");
            String awayLogo = teams.path("away").path("logo").asText("");
            
            Integer homeScore = goals.path("home").asInt(0);
            Integer awayScore = goals.path("away").asInt(0);
            
            String date = fixtureNode.path("date").asText("");
            String status = fixtureNode.path("status").path("short").asText("");

            if (dto instanceof BetResponseDTO) {
                BetResponseDTO r = (BetResponseDTO) dto;
                r.setHomeTeam(homeName);
                r.setHomeTeamLogo(homeLogo);
                r.setAwayTeam(awayName);
                r.setAwayTeamLogo(awayLogo);
                r.setHomeScore(homeScore);
                r.setAwayScore(awayScore);
                r.setMatchDate(date);
                r.setMatchStatus(status);
            } else if (dto instanceof BetViewAllDTO) {
                BetViewAllDTO v = (BetViewAllDTO) dto;
                v.setHomeTeam(homeName);
                v.setHomeTeamLogo(homeLogo);
                v.setAwayTeam(awayName);
                v.setAwayTeamLogo(awayLogo);
                v.setHomeScore(homeScore);
                v.setAwayScore(awayScore);
                
                // CRITICAL FIX: Pass the date string to the DTO
                v.setMatchDate(date); 
                v.setMatchStatus(status);
            }

        } catch (Exception e) {
            log.error("Failed to parse fixture JSON for bet enrichment", e);
        }
    }
}