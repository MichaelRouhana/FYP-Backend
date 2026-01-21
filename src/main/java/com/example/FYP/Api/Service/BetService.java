package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchSettings;
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

        String ticketId = UUID.randomUUID().toString();
        
        List<Bet> bets = new ArrayList<>();
        BigDecimal totalOdds = BigDecimal.ONE;
        
        for (var leg : betDTO.getLegs()) {
            Fixture fixture = fixtureRepository.findById(leg.getFixtureId())
                    .orElseThrow(() -> ApiRequestException.badRequest("Fixture not found"));

            MatchSettings settings = fixture.getMatchSettings();
            if (settings == null) {
                settings = MatchSettings.builder()
                        .allowBetting(true)
                        .allowBettingHT(false)
                        .showMatch(true)
                        .build();
                fixture.setMatchSettings(settings);
                fixtureRepository.save(fixture);
            }
            
            if (!Boolean.TRUE.equals(settings.getAllowBetting())) {
                throw ApiRequestException.badRequest("Betting is currently disabled for this match.");
            }
            
            String statusShort = fixture.getStatusShort();
            if (statusShort != null && ("HT".equalsIgnoreCase(statusShort) || 
                    "1H".equalsIgnoreCase(statusShort) || 
                    "2H".equalsIgnoreCase(statusShort))) {
                if (!Boolean.TRUE.equals(settings.getAllowBettingHT())) {
                    throw ApiRequestException.badRequest("Halftime betting is disabled for this match.");
                }
            }

            Bet bet = Bet.builder()
                    .marketType(leg.getMarketType())
                    .fixture(fixture)
                    .selection(leg.getSelection())
                    .stake(betDTO.getStake())
                    .odd(leg.getOdd())
                    .status(BetStatus.PENDING)
                    .user(user)
                    .ticketId(ticketId)
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

        List<Bet> ticketLegs;
        if (originalBet.getTicketId() != null && !originalBet.getTicketId().isEmpty()) {
            ticketLegs = betRepository.findByTicketId(originalBet.getTicketId());
            log.info("Found {} legs for ticket {}", ticketLegs.size(), originalBet.getTicketId());
        } else {
            ticketLegs = List.of(originalBet);
            log.debug("Bet {} has no ticketId, treating as single-leg bet", id);
        }

        BetResponseDTO response = new BetResponseDTO();
        response.setId(originalBet.getId());
        
        Double stake = ticketLegs.get(0).getStake();
        response.setStake(stake);
        
        BigDecimal totalOdds = BigDecimal.ONE;
        for (Bet legBet : ticketLegs) {
            if (legBet.getOdd() != null) {
                totalOdds = totalOdds.multiply(legBet.getOdd());
            }
        }
        response.setTotalOdds(totalOdds);
        
        if (stake != null) {
            response.setPotentialWinnings(totalOdds.multiply(BigDecimal.valueOf(stake)));
        } else {
            response.setPotentialWinnings(BigDecimal.ZERO);
        }

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
        
        response.setLegs(legResponses);

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
                
                v.setMatchDate(date); 
                v.setMatchStatus(status);
            }

        } catch (Exception e) {
            log.error("Failed to parse fixture JSON for bet enrichment", e);
        }
    }
}