package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Loader.Annotation.Feature;
import com.example.FYP.Api.Model.Patch.MatchPredictionSettingsPatchDTO;
import com.example.FYP.Api.Model.Patch.MatchSettingsPatchDTO;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Service.FixtureService;
import com.example.FYP.Api.Service.FixtureSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/fixtures")
@Validated
@Tag(name = "Fixture Controller", description = "Provides basic functionalities for fixtures")
@Feature
@RequiredArgsConstructor
public class FixtureController {

    private final FixtureService fixtureService;
    private final FixtureSyncService fixtureSyncService;

    @GetMapping("/all")
    @Operation(summary = "Get all fixtures", description = "Returns all fixtures including those hidden from users")
    public List<FixtureViewDTO> getAllFixtures() {
        return fixtureService.getAllFixtures();
    }

    @GetMapping("/public")
    @Operation(summary = "Get public fixtures", description = "Returns fixtures that are visible and allowed for betting")
    public List<FixtureViewDTO> getPublicFixtures() {
        return fixtureService.getPublicFixtures();
    }

    @GetMapping("/{fixtureId}/settings")
    public FixtureViewDTO.MatchSettingsView getFixtureMatchSettings(@PathVariable Long fixtureId) {
        return fixtureService.getMatchSettings(fixtureId);
    }

    @GetMapping("/{fixtureId}/prediction-settings")
    public FixtureViewDTO.MatchPredictionSettingsView getFixtureMatchPredictionSettings(@PathVariable Long fixtureId) {
        return fixtureService.getMatchPredictionSettings(fixtureId);
    }


    @GetMapping("/{fixtureId}/users")
    @Operation(summary = "Get users betting on fixture", description = "Returns a list of users who bet on this fixture with their total wagered amount")
    public List<com.example.FYP.Api.Model.View.UserBettingOnFixtureDTO> getUsersBettingOnFixture(@PathVariable Long fixtureId) {
        return fixtureService.getUsersBettingOnFixture(fixtureId);
    }




    @Operation(summary = "Update fixture's match prediction settings",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @PatchMapping("/{fixtureId}/prediction-settings")
    public ResponseEntity<?> patchMatchPredictionSettings(@PathVariable Long fixtureId,
                                                          @RequestBody MatchPredictionSettingsPatchDTO patchDTO) {
        fixtureService.patchMatchPredictionSettings(fixtureId, patchDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update fixture's match settings",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @PatchMapping("/{fixtureId}/settings")
    public ResponseEntity<?> patchMatchSettings(@PathVariable Long fixtureId,
                                                @RequestBody MatchSettingsPatchDTO patchDTO) {
        fixtureService.patchMatchSettings(fixtureId, patchDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync")
    @Operation(summary = "Manually sync fixtures", description = "Syncs fixtures for today and the next 7 days from Football-API")
    public ResponseEntity<String> syncFixtures() {
        try {
            for (int i = 0; i <= 7; i++) {
                String date = LocalDate.now().plusDays(i).toString();
                fixtureSyncService.syncFixtures(date);
            }
            return ResponseEntity.ok("Fixtures synced successfully for the next 7 days");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }
    }

    @PostMapping("/sync/{date}")
    @Operation(summary = "Sync fixtures for specific date", description = "Syncs fixtures for a specific date (YYYY-MM-DD)")
    public ResponseEntity<String> syncFixturesForDate(@PathVariable String date) {
        try {
            fixtureSyncService.syncFixtures(date);
            return ResponseEntity.ok("Fixtures synced for " + date);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }
    }
}
