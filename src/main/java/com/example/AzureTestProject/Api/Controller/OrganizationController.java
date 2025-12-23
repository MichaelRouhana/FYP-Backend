package com.example.AzureTestProject.Api.Controller;

import com.example.AzureTestProject.Api.Interceptor.Annotation.RequiredRole;
import com.example.AzureTestProject.Api.Loader.Annotation.Feature;
import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;
import com.example.AzureTestProject.Api.Model.Patch.OrganizationPatchDTO;
import com.example.AzureTestProject.Api.Model.Request.InvitationRequestDTO;
import com.example.AzureTestProject.Api.Model.Request.OrganizationRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.InvitationResponseDTO;
import com.example.AzureTestProject.Api.Model.Response.OrganizationResponseDTO;
import com.example.AzureTestProject.Api.Model.View.UserViewDTO;
import com.example.AzureTestProject.Api.Service.OrganizationService;
import com.example.AzureTestProject.Api.Service.UserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
@Validated
@Tag(name = "Organization Controller", description = "provides basic functionalities for organizations")
@Feature
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;


    @Operation(summary = "retrieve organization",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "retrieved organization", responseCode = "200"),
            })
    @GetMapping
    @RequiredRole({OrganizationRoles.OWNER, OrganizationRoles.MEMBER})
    public ResponseEntity<OrganizationResponseDTO> get(@RequestParam String organizationUUID) {
        return ResponseEntity.ok(organizationService.get(organizationUUID));
    }

    @Operation(summary = "assign tier",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            })
    @PostMapping("/tiers/assign")
    public ResponseEntity<Void> create(@RequestParam("organizationUUID") String organizationUUID,
                                                          String email,
                                                          List<String> tiers) {
        organizationService.assignTier(organizationUUID,email, tiers);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "create organization",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Organization Created", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrganizationRequestDTO.class))),
            })
    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> create(@RequestBody @Valid OrganizationRequestDTO organizationDTO) {
        return ResponseEntity.ok(organizationService.create(organizationDTO));
    }



    @Operation(summary = "delete organization",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Organization Deleted", responseCode = "200"),
            })
    @DeleteMapping()
    @RequiredRole({OrganizationRoles.OWNER})
    //@PreAuthorize("@userService.hasOrganizationRole(#organizationUUID, 'OWNER')")
    public ResponseEntity<Void> delete(@RequestParam @NotBlank String organizationUUID) {
        organizationService.delete(organizationUUID);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Invite to organization",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "User invited", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = InvitationRequestDTO.class))),
            })
    @PostMapping("/invite")
    @RequiredRole({OrganizationRoles.OWNER})
    public ResponseEntity<InvitationResponseDTO> invite(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID
            , @RequestBody @Valid InvitationRequestDTO invitationRequestDTO) {
        return ResponseEntity.ok(organizationService.invite(organizationUUID, invitationRequestDTO));
    }


    @Operation(summary = "Accept Invitation",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "User invited", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = InvitationRequestDTO.class))),
            })
    @PostMapping("/invitation/accept/{invitationUUID}")
    public ResponseEntity<Void> acceptInvitation(@PathVariable @NotBlank(message = "invitationUUID cannot be blank") String invitationUUID) {
        organizationService.acceptInvitation(invitationUUID);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "remove user",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "User removed", responseCode = "200"),
            })
    @DeleteMapping("/remove")
    @RequiredRole({OrganizationRoles.OWNER})
    public ResponseEntity<Void> removeUser(@RequestParam @NotBlank String organizationUUID,
                                           @RequestParam @NotBlank String email) {
        organizationService.removeUser(organizationUUID, email);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "retrieve Invitations",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "retrieved invitations", responseCode = "200"),
            })
    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationResponseDTO>> getAllInvitations() {
        return ResponseEntity.ok(organizationService.getInvitations());
    }


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
    @RequiredRole({OrganizationRoles.OWNER})
    public ResponseEntity<Void> assignRole(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                           @RequestParam("user") String user,
                                           @RequestParam("roles") List<OrganizationRoles> roles) {
        organizationService.assignRole(organizationUUID, user, roles);
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
    @RequiredRole({OrganizationRoles.OWNER})
    public ResponseEntity<Void> revokeRole(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                           @RequestParam("user") String user,
                                           @RequestParam("roles") List<OrganizationRoles> roles) {
        organizationService.revokeRole(organizationUUID, user, roles);
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
    public ResponseEntity<List<String>> getRoles(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                                 @RequestParam("user") String user) {
        return ResponseEntity.ok(organizationService.getRoles(organizationUUID, user));
    }


    @Operation(summary = "patch organization",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "organization patched", responseCode = "200"),
            })
    @PatchMapping
    @RequiredRole({OrganizationRoles.OWNER})
    public ResponseEntity<Void> patch(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                      @RequestBody OrganizationPatchDTO organizationPatchDTO) {
        organizationService.patch(organizationUUID, organizationPatchDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "me organization",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "user fetched", responseCode = "200"),
            })
    @GetMapping("/me")
    public ResponseEntity<UserViewDTO> me(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID) {
        return ResponseEntity.ok(organizationService.me(organizationUUID));
    }


}
