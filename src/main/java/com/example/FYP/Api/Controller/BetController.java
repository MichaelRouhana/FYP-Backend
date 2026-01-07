package com.example.FYP.Api.Controller;


import com.example.FYP.Api.Interceptor.Annotation.RequiredRole;
import com.example.FYP.Api.Loader.Annotation.Feature;
import com.example.FYP.Api.Model.Filter.BetFilterDTO;
import com.example.FYP.Api.Model.Request.BetRequestDTO;
import com.example.FYP.Api.Model.Response.BetResponseDTO;
import com.example.FYP.Api.Model.View.BetViewAllDTO;
import com.example.FYP.Api.Service.BetService;
import com.example.FYP.Api.Service.BetResolverService;
import com.example.FYP.Api.Util.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bets")
@Validated
@Tag(name = "Bet Controller", description = "provides basic functionalities for bet's")
@Feature
public class  BetController {

    @Autowired
    private BetService betService;

    @Autowired
    private BetResolverService betResolverService;




    @PostMapping
    public ResponseEntity<BetResponseDTO> create(@RequestBody @Valid BetRequestDTO betDTO) {
        return ResponseEntity.ok(betService.create(betDTO));
    }



    @Operation(summary = "retrieve bet's",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "bet's retrieved Successfully!", responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BetViewAllDTO.class)))
            })
    @GetMapping
    public ResponseEntity<PagedResponse<BetViewAllDTO>> getAll(Pageable pageable,
                                                               BetFilterDTO filter) {
        return ResponseEntity.ok(betService.getAll(pageable, filter));
    }

    @Operation(summary = "Manually trigger bet resolution for a specific fixture (Admin only)",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/resolve/{fixtureId}")
    public ResponseEntity<String> resolveFixtureBets(@PathVariable Long fixtureId) {
        try {
            betResolverService.resolveBetsForFixture(fixtureId);
            return ResponseEntity.ok("Bets resolved successfully for fixture " + fixtureId);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Failed to resolve bets: " + e.getMessage());
        }
    }

/*


    @Operation(summary = "retrieve bet",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "retrieved bet", responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BetResponseDTO.class))),
            })
    @GetMapping("/{betUUID}")
    public ResponseEntity<BetResponseDTO> get(@RequestParam("organizationUUID") String organizationUUID,
                                                    @PathVariable String betUUID) {
        return ResponseEntity.ok(betService.get(organizationUUID, betUUID));
    }



    @Operation(summary = "patch bet",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "bet patched", responseCode = "200"),
            })
    @PatchMapping("/{betUUID}")
    public ResponseEntity<Void> patch(@PathVariable @NotBlank(message = "betUUID cannot be blank") String betUUID,
                                      @RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                      @RequestBody @Valid BetPatchDTO betPatchDTO) {
        betService.patch(organizationUUID, betUUID, betPatchDTO);
        return ResponseEntity.ok().build();
    }*/
}
