package com.example.FYP.Api.Service;


import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Model.Filter.BetFilterDTO;
import com.example.FYP.Api.Model.Request.BetRequestDTO;
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
        Fixture fixture = fixtureRepository.findById(betDTO.getFixtureId())
                .orElseThrow(() -> ApiRequestException.badRequest("Fixture not found"));

        if(!fixture.getMatchSettings().getAllowBetting().equals(Boolean.TRUE)) {
            throw ApiRequestException.badRequest("cannot bet on this match");
        }

        // Validate stake
        if (betDTO.getStake() == null || betDTO.getStake() <= 0) {
            throw ApiRequestException.badRequest("Stake must be greater than 0");
        }

        // Get current user and reload from database to ensure we have latest balance
        User currentUser = securityContext.getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> ApiRequestException.badRequest("User not found"));
        
        // Check if user has sufficient balance
        Long stakeLong = betDTO.getStake() != null ? betDTO.getStake().longValue() : 0L;
        if (user.getPoints() == null || user.getPoints() < stakeLong) {
            throw ApiRequestException.badRequest("Insufficient balance. You have " + 
                (user.getPoints() != null ? user.getPoints() : 0) + " PTS but need " + stakeLong + " PTS");
        }

        // Deduct stake from user balance
        user.setPoints(user.getPoints() - stakeLong);
        userRepository.save(user);

        // Create bet
        Bet bet = Bet.builder()
                .marketType(betDTO.getMarketType())
                .fixture(fixture)
                .selection(betDTO.getSelection())
                .stake(betDTO.getStake())
                .odd(betDTO.getOdd())
                .status(BetStatus.PENDING)
                .user(user)
                .build();

        betRepository.save(bet);
        log.info("Bet created: ID={}, User={}, Stake={}, Balance after={}", 
            bet.getId(), user.getEmail(), betDTO.getStake(), user.getPoints());
        
        return modelMapper.map(bet, BetResponseDTO.class);
    }

    public PagedResponse<BetViewAllDTO> getAll(Pageable pageable, BetFilterDTO filter) {

        Specification<Bet> specification = Specification.where(
                GenericSpecification.<Bet>filterByFields(filter)
        );

        Page<Bet> purchaseOrderPage = betRepository.findAll(specification, pageable);

        Page<BetViewAllDTO> accountResponsePage = purchaseOrderPage.map(bet -> {
            BetViewAllDTO dto = modelMapper.map(bet, BetViewAllDTO.class);
            // Manually set fixtureId since ModelMapper might not map nested fields automatically
            if (bet.getFixture() != null) {
                dto.setFixtureId(bet.getFixture().getId());
            }
            return dto;
        });

        return PagedResponse.fromPage(accountResponsePage);
    }
}
