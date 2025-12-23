package com.example.AzureTestProject.Api.Controller;

import com.example.AzureTestProject.Api.Loader.Annotation.Feature;
import com.example.AzureTestProject.Api.Model.Filter.CommunityFilterDTO;
import com.example.AzureTestProject.Api.Model.Patch.CommunityPatchDTO;
import com.example.AzureTestProject.Api.Model.Request.CommunityRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.CommunityResponseDTO;
import com.example.AzureTestProject.Api.Service.CommunityService;
import com.example.AzureTestProject.Api.Util.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/communities")
@Validated
@Tag(name = "Community Controller", description = "provides basic functionalities for community's")
@Feature
public class  CommunityController {

    @Autowired
    private CommunityService communityService;

    @Operation(summary = "retrieve community",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "retrieved community", responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommunityResponseDTO.class))),
            })
    @GetMapping("/{communityId}")
    public ResponseEntity<CommunityResponseDTO> get(@PathVariable Long communityId) {
        return ResponseEntity.ok(communityService.get(communityId));
    }

    @Operation(summary = "create community",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "Community created", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CommunityResponseDTO.class))),
            })
    @PostMapping
    public ResponseEntity<CommunityResponseDTO> create(@RequestBody @Valid CommunityRequestDTO communityDTO) {
        return ResponseEntity.ok(communityService.create(communityDTO));
    }



    @Operation(summary = "join community",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "Community created", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CommunityResponseDTO.class))),
            })
    @PostMapping("/join/{communityId}")
    public ResponseEntity<?> join(@PathVariable Long communityId) {
        communityService.join(communityId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "leave community",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "Community created", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CommunityResponseDTO.class))),
            })
    @PostMapping("/leave/{communityId}")
    public ResponseEntity<?> leave(@PathVariable Long communityId) {
        communityService.leave(communityId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "delete community",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "community Deleted", responseCode = "200"),

            })
    @DeleteMapping("/{communityId}")
    public ResponseEntity<Void> delete(@PathVariable Long communityId) {

        communityService.delete(communityId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "retrieve community's",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "community's retrieved Successfully!", responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommunityResponseDTO.class)))
            })
    @GetMapping
    public ResponseEntity<PagedResponse<CommunityResponseDTO>> getAll(Pageable pageable,
                                                                      CommunityFilterDTO filter) {
        return ResponseEntity.ok(communityService.getAll(pageable, filter));
    }



    @Operation(summary = "retrieve community's leaderboard",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/{communityId}/leaderboard")
    public ResponseEntity<?> leaderboard(@PathVariable Long communityId) {
        return ResponseEntity.ok(communityService.leaderboard(communityId));
    }

    @Operation(summary = "retrieve community's members",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)

            })
    @GetMapping("/{communityId}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long communityId) {
        return ResponseEntity.ok(communityService.getMembers(communityId));
    }


    @Operation(summary = "patch community",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "community patched", responseCode = "200"),
            })
    @PatchMapping("/{communityId}")
    public ResponseEntity<Void> patch(@PathVariable @NotBlank(message = "communityId cannot be blank") Long communityId,
                                      @RequestBody @Valid CommunityPatchDTO communityPatchDTO) {
        communityService.patch(communityId, communityPatchDTO);
        return ResponseEntity.ok().build();
    }
}
