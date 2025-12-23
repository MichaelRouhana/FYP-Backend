package com.example.AzureTestProject.Api.Controller;

import com.example.AzureTestProject.Api.Loader.Annotation.Feature;
import com.example.AzureTestProject.Api.Model.Patch.MatchPredictionSettingsPatchDTO;
import com.example.AzureTestProject.Api.Model.Patch.MatchSettingsPatchDTO;
import com.example.AzureTestProject.Api.Model.View.FixtureViewDTO;
import com.example.AzureTestProject.Api.Service.FixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fixtures")
@Validated
@Tag(name = "Fixture Controller", description = "Provides basic functionalities for fixtures")
@Feature
@RequiredArgsConstructor
public class FixtureController {

    private final FixtureService fixtureService;

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
}
