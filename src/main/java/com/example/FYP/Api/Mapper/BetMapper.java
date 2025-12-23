package com.example.FYP.Api.Mapper;


import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Model.Response.BetResponseDTO;
import com.example.FYP.Api.Model.View.BetViewAllDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BetMapper {

    BetMapper INSTANCE = Mappers.getMapper(BetMapper.class);

    @Mapping(source = "fixture.id", target = "fixtureId")
    BetViewAllDTO toView(Bet bet);


    BetResponseDTO toDto(Bet bet);
}
