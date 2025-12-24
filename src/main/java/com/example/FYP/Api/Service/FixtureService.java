package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchPredictionSettings;
import com.example.FYP.Api.Entity.MatchSettings;
import com.example.FYP.Api.Mapper.FixtureMapper;
import com.example.FYP.Api.Model.Patch.MatchPredictionSettingsPatchDTO;
import com.example.FYP.Api.Model.Patch.MatchSettingsPatchDTO;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
import com.example.FYP.Api.Repository.FixtureRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final ModelMapper modelMapper;
    private final FixtureMapper fixtureMapper;
    private final ObjectMapper objectMapper;

    public List<FixtureViewDTO> getAllFixtures() {
        return fixtureMapper.toDTOs(fixtureRepository.findAll());
    }

    @Cacheable(value = "publicFixtures")
    public List<FixtureViewDTO> getPublicFixtures() {
        log.info("Fetching public fixtures from database (cache miss)");
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        
        return fixtureRepository.findAll().stream()
                .filter(fixture ->
                        fixture.getMatchSettings() != null &&
                                Boolean.TRUE.equals(fixture.getMatchSettings().getShowMatch()) &&
                                Boolean.TRUE.equals(fixture.getMatchSettings().getAllowBetting())
                )
                // Filter by date - only show matches from today onwards (up to 7 days)
                .filter(fixture -> {
                    try {
                        JsonNode rawJson = objectMapper.readTree(fixture.getRawJson());
                        String dateStr = rawJson.path("fixture").path("date").asText();
                        if (dateStr == null || dateStr.isEmpty()) return false;
                        
                        // Parse ISO date
                        Instant instant = Instant.parse(dateStr);
                        LocalDate fixtureDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                        
                        // Include fixtures from yesterday (for recently finished) up to 7 days ahead
                        return !fixtureDate.isBefore(today.minusDays(1)) && !fixtureDate.isAfter(weekFromNow);
                    } catch (Exception e) {
                        log.warn("Failed to parse fixture date: {}", e.getMessage());
                        return false;
                    }
                })
                // Sort by date (earliest first)
                .sorted((f1, f2) -> {
                    try {
                        JsonNode json1 = objectMapper.readTree(f1.getRawJson());
                        JsonNode json2 = objectMapper.readTree(f2.getRawJson());
                        String date1 = json1.path("fixture").path("date").asText();
                        String date2 = json2.path("fixture").path("date").asText();
                        return date1.compareTo(date2);
                    } catch (Exception e) {
                        return 0;
                    }
                })
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
