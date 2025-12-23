package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.Fixture;
import com.example.AzureTestProject.Api.Entity.MatchPredictionSettings;
import com.example.AzureTestProject.Api.Entity.MatchSettings;
import com.example.AzureTestProject.Api.Model.Patch.MatchPredictionSettingsPatchDTO;
import com.example.AzureTestProject.Api.Model.Patch.MatchSettingsPatchDTO;
import com.example.AzureTestProject.Api.Model.View.FixtureViewDTO;
import com.example.AzureTestProject.Api.Repository.FixtureRepository;
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

    public List<FixtureViewDTO> getAllFixtures() {
        return fixtureRepository.findAll()
                .stream()
                .map(fixture -> modelMapper.map(fixture, FixtureViewDTO.class))
                .toList();
    }

    public List<FixtureViewDTO> getPublicFixtures() {
        return fixtureRepository.findAll()
                .stream()
                .filter(fixture ->
                        fixture.getMatchSettings() != null &&
                                Boolean.TRUE.equals(fixture.getMatchSettings().getShowMatch()) &&
                                Boolean.TRUE.equals(fixture.getMatchSettings().getAllowBetting())
                )
                .map(fixture -> modelMapper.map(fixture, FixtureViewDTO.class))
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
