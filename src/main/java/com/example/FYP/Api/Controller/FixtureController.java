package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Loader.Annotation.Feature;
import com.example.FYP.Api.Model.Patch.MatchPredictionSettingsPatchDTO;
import com.example.FYP.Api.Model.Patch.MatchSettingsPatchDTO;
import com.example.FYP.Api.Model.View.FixtureViewDTO;
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




    @Operation(summary = "patch fixture's match prediction settings",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @PatchMapping("/matchPredictionSettings")
    public ResponseEntity<?> patchMatchPredictionSettings(@RequestParam Long fixtureId,
                                                          @RequestBody MatchPredictionSettingsPatchDTO patchDTO) {
        fixtureService.patchMatchPredictionSettings(fixtureId, patchDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "patch fixture's match prediction settings",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @PatchMapping("/matchSettings")
    public ResponseEntity<?> patchMatchSettings(@RequestParam Long fixtureId,
                                                @RequestBody MatchSettingsPatchDTO patchDTO) {
        fixtureService.patchMatchSettings(fixtureId, patchDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync")
    @Operation(summary = "Manually sync fixtures", description = "Syncs fixtures for today and the next 7 days from Football-API")
    public ResponseEntity<String> syncFixtures() {
        try {
            // Sync today and next 7 days
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
