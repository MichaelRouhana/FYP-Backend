package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Bet;
import com.example.FYP.Api.Entity.Fixture;
import com.example.FYP.Api.Entity.MatchPredictionSettings;
import com.example.FYP.Api.Entity.MatchSettings;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Mapper.FixtureMapper;
import com.example.FYP.Api.Model.Constant.Role;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Model.Patch.MatchPredictionSettingsPatchDTO;
import com.example.FYP.Api.Model.Patch.MatchSettingsPatchDTO;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
import com.example.FYP.Api.Model.View.UserBettingOnFixtureDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final ModelMapper modelMapper;
    private final FixtureMapper fixtureMapper;
    private final ObjectMapper objectMapper;
    private final SecurityContext securityContext;

    public List<FixtureViewDTO> getAllFixtures() {
        // Check if current user is admin
        boolean isAdmin = false;
        try {
            User currentUser = securityContext.getCurrentUser();
            isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getRole() == Role.ADMIN);
        } catch (Exception e) {
            // If user is not authenticated or not found, treat as non-admin
            log.debug("User not authenticated or not found, treating as non-admin");
        }
        
        final boolean finalIsAdmin = isAdmin;
        
        return fixtureRepository.findAll().stream()
                .filter(fixture -> {
                    // If admin, show all fixtures (including hidden ones)
                    if (finalIsAdmin) {
                        return true;
                    }
                    
                    // If non-admin or guest, only show fixtures where showMatch == true
                    MatchSettings settings = fixture.getMatchSettings();
                    if (settings == null) {
                        // If no settings exist, default to showing (for backward compatibility)
                        return true;
                    }
                    return Boolean.TRUE.equals(settings.getShowMatch());
                })
                .map(fixtureMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "publicFixtures")
    public List<FixtureViewDTO> getPublicFixtures() {
        log.info("Fetching public fixtures from database (cache miss)");
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        
        return fixtureRepository.findAll().stream()
                .filter(fixture -> {
                    // Must have MatchSettings and showMatch must be true
                    if (fixture.getMatchSettings() == null || 
                        !Boolean.TRUE.equals(fixture.getMatchSettings().getShowMatch())) {
                        return false;
                    }
                    
                    // Check if match is finished
                    boolean isFinished = false;
                    try {
                        JsonNode rawJson = objectMapper.readTree(fixture.getRawJson());
                        String statusShort = rawJson.path("fixture").path("status").path("short").asText();
                        isFinished = Set.of("FT", "AET", "PEN", "WO", "ABD", "AWD").contains(statusShort);
                    } catch (Exception e) {
                        log.warn("Failed to parse fixture status: {}", e.getMessage());
                    }
                    
                    // For finished matches: only require showMatch == true
                    // For upcoming/live matches: require both showMatch == true AND allowBetting == true
                    if (isFinished) {
                        return true; // Show finished matches if showMatch is true
                    } else {
                        return Boolean.TRUE.equals(fixture.getMatchSettings().getAllowBetting());
                    }
                })
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
            fixture.setMatchSettings(MatchSettings.builder().build());
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
            fixture.setMatchPredictionSettings(MatchPredictionSettings.builder().build());
        }

        // Map only non-null fields
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(patchDTO, fixture.getMatchPredictionSettings());

        fixtureRepository.save(fixture);
    }

    public FixtureViewDTO.MatchSettingsView getMatchSettings(Long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EntityNotFoundException("Fixture not found: " + fixtureId));

        MatchSettings matchSettings = fixture.getMatchSettings() != null 
                ? fixture.getMatchSettings() 
                : MatchSettings.builder().build();

        // Map entity to DTO
        return modelMapper.map(matchSettings, FixtureViewDTO.MatchSettingsView.class);
    }

    public FixtureViewDTO.MatchPredictionSettingsView getMatchPredictionSettings(Long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EntityNotFoundException("Fixture not found: " + fixtureId));

        MatchPredictionSettings matchPredictionSettings = fixture.getMatchPredictionSettings() != null
                ? fixture.getMatchPredictionSettings()
                : MatchPredictionSettings.builder().build();

        // Map entity to DTO
        return modelMapper.map(matchPredictionSettings, FixtureViewDTO.MatchPredictionSettingsView.class);
    }

    public List<UserBettingOnFixtureDTO> getUsersBettingOnFixture(Long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EntityNotFoundException("Fixture not found: " + fixtureId));

        // Group bets by user and calculate total wagered
        Map<User, Double> userTotalWagered = fixture.getBetsSet().stream()
                .filter(bet -> bet.getUser() != null && bet.getStake() != null)
                .collect(Collectors.groupingBy(
                        Bet::getUser,
                        Collectors.summingDouble(Bet::getStake)
                ));

        // Convert to DTOs
        return userTotalWagered.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    Double totalWagered = entry.getValue();
                    
                    UserBettingOnFixtureDTO dto = new UserBettingOnFixtureDTO();
                    dto.setUserId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setAvatar(user.getPfp());
                    dto.setTotalWagered(totalWagered);
                    
                    return dto;
                })
                .sorted((a, b) -> Double.compare(b.getTotalWagered(), a.getTotalWagered())) // Sort by total wagered descending
                .collect(Collectors.toList());
    }
}
