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
        // Fix: Use longValue() only if it's a wrapper, typically RequestDTO uses Double or Integer
        long stakeLong = betDTO.getStake().longValue(); 
        if (user.getPoints() == null || user.getPoints() < stakeLong) {
            throw ApiRequestException.badRequest("Insufficient balance. You have " + 
                (user.getPoints() != null ? user.getPoints() : 0) + " PTS but need " + stakeLong + " PTS");
        }

        List<Bet> bets = new ArrayList<>();
        BigDecimal totalOdds = BigDecimal.ONE;
        
        for (var leg : betDTO.getLegs()) {
            Fixture fixture = fixtureRepository.findById(leg.getFixtureId())
                    .orElseThrow(() -> ApiRequestException.badRequest("Fixture not found: " + leg.getFixtureId()));

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

        // Deduct stake from user balance
        user.setPoints(user.getPoints() - stakeLong);
        userRepository.save(user);

        // Save all bet legs
        betRepository.saveAll(bets);
        
        BigDecimal potentialWinnings = BigDecimal.valueOf(betDTO.getStake()).multiply(totalOdds);
        
        log.info("Bet created: {} legs, User={}, Stake={}, Total Odds={}", 
            bets.size(), user.getEmail(), betDTO.getStake(), totalOdds);

        // Build response
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
            
            // Map Fixture Details for History
            if (bet.getFixture() != null) {
                Fixture f = bet.getFixture();
                dto.setFixtureId(f.getId());
                
                // Map Team Details (Fixes "Team A vs Team B" issue)
                if (f.getRawJson() != null && f.getRawJson().getTeams() != null) {
                    if (f.getRawJson().getTeams().getHome() != null) {
                        dto.setHomeTeam(f.getRawJson().getTeams().getHome().getName());
                        dto.setHomeTeamLogo(f.getRawJson().getTeams().getHome().getLogo());
                    }
                    if (f.getRawJson().getTeams().getAway() != null) {
                        dto.setAwayTeam(f.getRawJson().getTeams().getAway().getName());
                        dto.setAwayTeamLogo(f.getRawJson().getTeams().getAway().getLogo());
                    }
                    // Map Scores if available
                    if (f.getRawJson().getGoals() != null) {
                        dto.setHomeScore(f.getRawJson().getGoals().getHome());
                        dto.setAwayScore(f.getRawJson().getGoals().getAway());
                    }
                    // Map Match Date
                    if (f.getRawJson().getFixture() != null && f.getRawJson().getFixture().getDate() != null) {
                        // Assuming OffsetDateTimeToStringConverter handles this, or map manually if needed
                        // dto.setMatchDate(...) 
                    }
                }
            }
            
            if (bet.getCreatedDate() != null) {
                dto.setCreatedDate(bet.getCreatedDate());
            }
            return dto;
        });

        return PagedResponse.fromPage(responsePage);
    }

    /**
     * Fixes Frontend Crash: GET /bets/{id}
     */
    public BetResponseDTO getBetById(Long id) {
        Bet bet = betRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found with id: " + id));

        // Use ModelMapper
        BetResponseDTO response = modelMapper.map(bet, BetResponseDTO.class);

        // Manually Populate Fixture Data to ensure Frontend displays Real Names
        if (bet.getFixture() != null) {
            Fixture f = bet.getFixture();
            response.setFixtureId(f.getId());
            
            if (f.getRawJson() != null && f.getRawJson().getTeams() != null) {
                 var teams = f.getRawJson().getTeams();
                 if (teams.getHome() != null) {
                     response.setHomeTeam(teams.getHome().getName());
                     response.setHomeTeamLogo(teams.getHome().getLogo());
                 }
                 if (teams.getAway() != null) {
                     response.setAwayTeam(teams.getAway().getName());
                     response.setAwayTeamLogo(teams.getAway().getLogo());
                 }
            }
            if (f.getRawJson() != null && f.getRawJson().getGoals() != null) {
                 response.setHomeScore(f.getRawJson().getGoals().getHome());
                 response.setAwayScore(f.getRawJson().getGoals().getAway());
            }
        }
        
        return response;
    }
}