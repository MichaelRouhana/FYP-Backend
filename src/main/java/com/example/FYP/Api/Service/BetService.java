package com.example.FYP.Api.Service;


import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Model.Filter.BetFilterDTO;
import com.example.FYP.Api.Model.Request.BetRequestDTO;
import com.example.FYP.Api.Model.Response.BetResponseDTO;
import com.example.FYP.Api.Model.View.BetViewAllDTO;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.FixtureRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;
    private final ModelMapper modelMapper;
    private final SecurityContext securityContext;
    private final FixtureRepository fixtureRepository;

    public BetResponseDTO create(BetRequestDTO betDTO) {
        Fixture fixture = fixtureRepository.findById(betDTO.getFixtureId())
                .orElseThrow();

        if(!fixture.getMatchSettings().getAllowBetting().equals(Boolean.TRUE)) throw ApiRequestException.badRequest("cannot bet on this match");
     //   if(!fixture.getMatchSettings().getAllowBettingHT().equals(Boolean.TRUE)) throw ApiRequestException.badRequest("cannot bet on this match");

        Bet bet = Bet.builder()
                .marketType(betDTO.getMarketType())
                .fixture(fixture)
                .selection(betDTO.getSelection())
                .status(BetStatus.PENDING)
                .user(securityContext.getCurrentUser())
                .build();

        betRepository.save(bet);
        return modelMapper.map(bet, BetResponseDTO.class);
    }

    public PagedResponse<BetViewAllDTO> getAll(Pageable pageable, BetFilterDTO filter) {

        Specification<Bet> specification = Specification.where(
                GenericSpecification.<Bet>filterByFields(filter)
        );

        Page<Bet> purchaseOrderPage = betRepository.findAll(specification, pageable);

        Page<BetViewAllDTO> accountResponsePage = purchaseOrderPage.map(account -> modelMapper.map(account, BetViewAllDTO.class));

        return PagedResponse.fromPage(accountResponsePage);
    }
}
