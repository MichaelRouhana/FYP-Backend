package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Interceptor.Annotation.RequiredRole;
import com.example.FYP.Api.Loader.Annotation.Feature;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.FYP.Api.Model.Constant.CommunityRoles;
import com.example.FYP.Api.Model.Filter.CommunityFilterDTO;
import com.example.FYP.Api.Model.Patch.CommunityPatchDTO;
import com.example.FYP.Api.Model.Request.CommunityRequestDTO;
import com.example.FYP.Api.Model.CommunityMessageDTO;
import com.example.FYP.Api.Model.Response.CommunityResponseDTO;
import com.example.FYP.Api.Model.View.CommunityViewDTO;
import com.example.FYP.Api.Messaging.WebSocket.CommunityMessage;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Repository.CommunityMessageRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Service.CommunityService;
import com.example.FYP.Api.Util.PagedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/communities")
@Validated
@Tag(name = "Community Controller", description = "provides basic functionalities for community's")
@Feature
public class  CommunityController {

    @Autowired
    private CommunityService communityService;

    // 1. Inject the Message Repository
    @Autowired
    private CommunityMessageRepository messageRepository;

    @Autowired
    private SecurityContext securityContext;

    @Autowired
    private ObjectMapper objectMapper;



    @Operation(summary = "Assign role",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "user", description = "user", required = true),
                    @Parameter(name = "roles", description = "roles", required = true)
            },
            responses = {
                    @ApiResponse(description = "Role(s) assigned", responseCode = "200"),
            })
    @PostMapping("/roles/assign")
   // @RequiredRole({CommunityRoles.OWNER, CommunityRoles.MODERATOR})
    public ResponseEntity<Void> assignRole(@RequestParam Long communityId,
                                           @RequestParam("user") String user,
                                           @RequestParam("roles") List<CommunityRoles> roles) {
        communityService.assignRole(communityId, user, roles);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Revoke role",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),

                    @Parameter(name = "user", description = "user", required = true),
                    @Parameter(name = "roles", description = "roles", required = true)


            },
            responses = {
                    @ApiResponse(description = "Role(s) revoked", responseCode = "200"),
            })
    @PostMapping("/roles/revoke")
   // @RequiredRole({CommunityRoles.OWNER, CommunityRoles.MODERATOR})
    public ResponseEntity<Void> revokeRole(@RequestParam Long communityId,
                                           @RequestParam("user") String user,
                                           @RequestParam("roles") List<CommunityRoles> roles) {
        communityService.revokeRole(communityId, user, roles);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "get roles",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "user", description = "user", required = true)

            },
            responses = {
                    @ApiResponse(description = "Role(s) retrieved", responseCode = "200"),
            })
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles(@RequestParam Long communityId,
                                                 @RequestParam("user") String user) {
        return ResponseEntity.ok(communityService.getRoles(communityId, user));
    }


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
    public ResponseEntity<CommunityViewDTO> get(@PathVariable Long communityId) {
        return ResponseEntity.ok(communityService.get(communityId));
    }

    @Operation(summary = "create community (Admin only)",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "data",
                            description = "Community data as JSON",
                            required = true,
                            in = ParameterIn.QUERY),
                    @Parameter(name = "file",
                            description = "Community logo image file (optional)",
                            required = false,
                            in = ParameterIn.QUERY)
            },
            responses = {
                    @ApiResponse(description = "Community created", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CommunityResponseDTO.class))),
            })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommunityResponseDTO> create(
            @RequestPart("data") String data,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        try {
            // Manually parse the JSON string since React Native sends it as text/plain
            CommunityRequestDTO communityDTO = objectMapper.readValue(data, CommunityRequestDTO.class);
            return ResponseEntity.ok(communityService.create(communityDTO, file, request));
        } catch (JsonProcessingException e) {
            throw ApiRequestException.badRequest("Invalid JSON format in community data: " + e.getMessage());
        } catch (Exception e) {
            throw ApiRequestException.badRequest("Failed to parse community data: " + e.getMessage());
        }
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

            })
    @PostMapping("/leave/{communityId}")
    public ResponseEntity<?> leave(@PathVariable Long communityId) {
        communityService.leave(communityId);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "leave community",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)

            })
    //@RequiredRole({CommunityRoles.OWNER, CommunityRoles.MODERATOR})
    @PostMapping("/kick/{communityId}")
    public ResponseEntity<?> kick(@PathVariable Long communityId,@RequestParam String email) {
        communityService.kick(communityId, email);
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
    @RequiredRole({CommunityRoles.OWNER})
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

    @Operation(summary = "Get user's joined communities",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "User's joined communities retrieved", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CommunityResponseDTO.class)))
            })
    @GetMapping("/my")
    public ResponseEntity<List<CommunityResponseDTO>> getMyCommunities() {
        User currentUser = securityContext.getCurrentUser();
        List<CommunityResponseDTO> communities = communityService.getJoinedCommunities(currentUser.getId());
        return ResponseEntity.ok(communities);
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
    //@RequiredRole({CommunityRoles.OWNER, CommunityRoles.MODERATOR})
    @PatchMapping("/{communityId}")
    public ResponseEntity<Void> patch(@PathVariable @NotBlank(message = "communityId cannot be blank") Long communityId,
                                      @RequestBody @Valid CommunityPatchDTO communityPatchDTO) {
        communityService.patch(communityId, communityPatchDTO);
        return ResponseEntity.ok().build();
    }

    // 2. Add this NEW Endpoint
    @Operation(summary = "Get community messages",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token",
                            required = true,
                            in = ParameterIn.HEADER)
            })
    @GetMapping("/{communityId}/messages")
    public ResponseEntity<List<CommunityMessageDTO>> getMessages(@PathVariable Long communityId) {
        List<CommunityMessage> messages = messageRepository.findByCommunityIdOrderBySentAtAsc(communityId);
        
        // Convert Database Entities to DTOs for the frontend
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        List<CommunityMessageDTO> dtos = messages.stream()
                .map(msg -> {
                    CommunityMessageDTO dto = new CommunityMessageDTO();
                    dto.setId(msg.getId());
                    dto.setContent(msg.getContent());
                    dto.setSenderUsername(msg.getSender().getUsername());
                    dto.setSenderEmail(msg.getSender().getEmail()); // Include email for identity verification
                    dto.setSenderId(msg.getSender().getId());
                    dto.setSentAt(msg.getSentAt() != null ? msg.getSentAt().format(formatter) : null);
                    return dto;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
