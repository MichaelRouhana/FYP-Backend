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

        List<Bet> bets = new ArrayList<>();
        BigDecimal totalOdds = BigDecimal.ONE;
        
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
                    .stake(betDTO.getStake())
                    .odd(leg.getOdd())
                    .status(BetStatus.PENDING)
                    .user(user)
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
        Bet bet = betRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found with id: " + id));

        // 1. Manually Build the Response (ModelMapper is unreliable here)
        BetResponseDTO response = new BetResponseDTO();
        response.setId(bet.getId());
        response.setStake(bet.getStake());
        response.setStatus(bet.getStatus());
        
        // 2. Map Odds & Calculations
        BigDecimal oddVal = bet.getOdd() != null ? bet.getOdd() : BigDecimal.ONE;
        response.setTotalOdds(oddVal);
        
        if (bet.getStake() != null) {
            response.setPotentialWinnings(oddVal.multiply(BigDecimal.valueOf(bet.getStake())));
        } else {
            response.setPotentialWinnings(BigDecimal.ZERO);
        }

        // 3. Construct the "Leg" (Since DB stores single bets as rows, we wrap it in a list)
        BetLegResponseDTO leg = new BetLegResponseDTO();
        leg.setId(bet.getId());
        if (bet.getFixture() != null) leg.setFixtureId(bet.getFixture().getId());
        leg.setMarketType(bet.getMarketType());
        leg.setSelection(bet.getSelection());
        leg.setOdd(bet.getOdd());
        leg.setStatus(bet.getStatus());
        
        response.setLegs(List.of(leg)); // <--- Crucial: Frontend needs this list

        // 4. Enrich with Fixture Data (Teams, Scores, Logos)
        if (bet.getFixture() != null) {
            Fixture f = bet.getFixture();
            response.setFixtureId(f.getId());
            enrichDtoWithFixtureData(response, f.getRawJson());
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