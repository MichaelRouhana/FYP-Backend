package com.example.AzureTestProject.Api.Controller;


import com.example.AzureTestProject.Api.Entity.Project;
import com.example.AzureTestProject.Api.Interceptor.Annotation.RequiredRole;
import com.example.AzureTestProject.Api.Loader.Annotation.Feature;
import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;
import com.example.AzureTestProject.Api.Model.Patch.ProjectPatchDTO;
import com.example.AzureTestProject.Api.Model.Request.ProjectRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.ProjectResponseDTO;
import com.example.AzureTestProject.Api.Service.ProjectService;
import com.example.AzureTestProject.Api.Util.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@Tag(name = "Project Controller", description = "provides basic functionalities for projects")
@RequiredRole({OrganizationRoles.OWNER, OrganizationRoles.MEMBER})
@Validated
@Feature
public class ProjectController {

    @Autowired
    private ProjectService projectService;


    @Operation(summary = "retrieve project",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true)
            },
            responses = {
                    @ApiResponse(description = "retrieved project", responseCode = "200"),
            })
    @GetMapping("/{projectUUID}")
    public ResponseEntity<Project> getProject(@PathVariable String projectUUID,
                                              @RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID) {
        return ResponseEntity.ok(projectService.get(organizationUUID, projectUUID));
    }

    @Operation(summary = "create a Project",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Project Created", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProjectRequestDTO.class))),
            })
    @PostMapping
    public ResponseEntity<ProjectResponseDTO> create(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                                     @RequestBody @Valid ProjectRequestDTO projectDTO) {
        return ResponseEntity.ok(projectService.create(organizationUUID, projectDTO));
    }


    @Operation(summary = "delete a Project",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "projectUUID", description = "projectUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "Project deleted", responseCode = "200"),
            })
    @DeleteMapping("/{projectUUID}")
    public ResponseEntity<Void> delete(@PathVariable @NotBlank(message = "projectUUID cannot be blank") String projectUUID,
                                       @RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID) {
        projectService.delete(organizationUUID, projectUUID);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "get projects",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "projects retrieved", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Pageable.class))),
            })
    @GetMapping
    public ResponseEntity<PagedResponse<Project>> getProjects(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                                              Pageable pageable) {
        return ResponseEntity.ok(projectService.getAll(organizationUUID, pageable));
    }

    @Operation(summary = "Add user to project",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "email", description = "email", required = true),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),
                    @Parameter(name = "projectUUID", description = "projectUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "user added", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Boolean.class))),
            })
    @PostMapping("/add/user")
    public ResponseEntity<Boolean> addMember(@RequestParam("email") @Email @NotBlank(message = "email cannot be blank") String email,
                                             @RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                             @RequestParam("projectUUID") @NotBlank(message = "projectUUID cannot be blank") String projectUUID) {
        return ResponseEntity.ok(projectService.addMember(organizationUUID, projectUUID, email));
    }


    @Operation(summary = "remove user from project",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "email", description = "email", required = true),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),
                    @Parameter(name = "projectUUID", description = "projectUUID", required = true)

            },
            responses = {
                    @ApiResponse(description = "user removed ", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Boolean.class))),
            })
    @DeleteMapping("/remove/user")
    public ResponseEntity<Boolean> removeMember(@RequestParam("email") @Email @NotBlank(message = "email cannot be blank") String email,
                                                @RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                                @RequestParam("projectUUID") @NotBlank(message = "projectUUID cannot be blank") String projectUUID) {
        return ResponseEntity.ok(projectService.removeMember(organizationUUID, projectUUID, email));
    }


    @Operation(summary = "patch project",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "organizationUUID", description = "organizationUUID", required = true),

            },
            responses = {
                    @ApiResponse(description = "project patched", responseCode = "200"),
            })
    @PatchMapping("/{projectUUID}")
    public ResponseEntity<Void> patchSupplier(@PathVariable String projectUUID,
                                              @RequestParam("organizationUUID") @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                              @RequestBody @Valid ProjectPatchDTO projectPatchDTO) {
        projectService.patch(organizationUUID, projectUUID, projectPatchDTO);
        return ResponseEntity.ok().build();
    }


}
