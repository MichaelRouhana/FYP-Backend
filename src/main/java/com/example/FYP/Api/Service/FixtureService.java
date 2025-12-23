package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchPredictionSettings;
import com.example.FYP.Api.Entity.MatchSettings;
import com.example.FYP.Api.Mapper.FixtureMapper;
import com.example.FYP.Api.Model.Patch.MatchPredictionSettingsPatchDTO;
import com.example.FYP.Api.Model.Patch.MatchSettingsPatchDTO;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
import com.example.FYP.Api.Repository.FixtureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final ModelMapper modelMapper;
    private final FixtureMapper fixtureMapper;

    public List<FixtureViewDTO> getAllFixtures() {
        return fixtureMapper.toDTOs(fixtureRepository.findAll());
    }

    public List<FixtureViewDTO> getPublicFixtures() {
        return fixtureRepository.findAll().stream()
                .filter(fixture ->
                        fixture.getMatchSettings() != null &&
                                Boolean.TRUE.equals(fixture.getMatchSettings().getShowMatch()) &&
                                Boolean.TRUE.equals(fixture.getMatchSettings().getAllowBetting())
                )
                .map(fixtureMapper::toDTO)
                .toList();
    }

    public void patchMatchSettings(Long fixtureId, MatchSettingsPatchDTO patchDTO) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EntityNotFoundException("Fixture not found: " + fixtureId));

        if (fixture.getMatchSettings() == null) {
            fixture.setMatchSettings(new MatchSettings());
        }

        // Map only non-null fields
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(patchDTO, fixture.getMatchSettings());

        fixtureRepository.save(fixture);
    }

    public void patchMatchPredictionSettings(Long fixtureId, MatchPredictionSettingsPatchDTO patchDTO) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EntityNotFoundException("Fixture not found: " + fixtureId));

        if (fixture.getMatchPredictionSettings() == null) {
            fixture.setMatchPredictionSettings(new MatchPredictionSettings());
        }

        // Map only non-null fields
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(patchDTO, fixture.getMatchPredictionSettings());

        fixtureRepository.save(fixture);
    }
}
