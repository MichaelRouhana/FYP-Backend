package com.example.FYP.Api.Mapper;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchPredictionSettings;
import com.example.FYP.Api.Entity.MatchSettings;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FixtureMapper {

    ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(source = "matchPredictionSettings", target = "matchPredictionSettings")
    @Mapping(source = "matchSettings", target = "matchSettings")
    @Mapping(source = "rawJson", target = "rawJson", qualifiedByName = "toJsonNode")
    @Mapping(source = "bets", target = "bets")
    FixtureViewDTO toDTO(Fixture fixture);

    List<FixtureViewDTO> toDTOs(List<Fixture> fixtures);

    @Named("toJsonNode")
    default JsonNode toJsonNode(String rawJson) {
        try {
            return objectMapper.readTree(rawJson);
        } catch (Exception e) {
            return null;
        }
    }

    FixtureViewDTO.MatchPredictionSettingsView toMatchPredictionSettingsView(MatchPredictionSettings entity);
    FixtureViewDTO.MatchSettingsView toMatchSettingsView(MatchSettings entity);
}
