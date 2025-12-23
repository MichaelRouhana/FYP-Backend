package com.example.AzureTestProject.Api.Controller;

import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Exception.ApiRequestException;
import com.example.AzureTestProject.Api.Interceptor.Annotation.RequiredRole;
import com.example.AzureTestProject.Api.Loader.Annotation.Feature;
import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;
import com.example.AzureTestProject.Api.Model.Filter.UserFilterDTO;
import com.example.AzureTestProject.Api.Model.Request.LoginRequestDTO;
import com.example.AzureTestProject.Api.Model.Request.SignUpRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.JwtResponseDTO;
import com.example.AzureTestProject.Api.Model.Response.OrganizationViewResponseDTO;
import com.example.AzureTestProject.Api.Model.View.UserViewDTO;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import com.example.AzureTestProject.Api.Security.SecurityContext;
import com.example.AzureTestProject.Api.Service.UserService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Controller", description = "provides basic functionalities for users")
@Feature
public class UserController {


    private final UserService userService;
    private final SecurityContext securityContext;

    @Operation(summary = "login user", responses = {
            @ApiResponse(description = "User found", responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDTO.class))),
            @ApiResponse(description = "User not found", responseCode = "404")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(userService.signin(loginRequestDTO));

    }

    private final UserRepository userRepository;
    @PostMapping("/setPoints")
    public ResponseEntity<?> points(@RequestParam Long points) {
        User user = securityContext.getCurrentUser();
        user.setPoints(points);
        userRepository.save(user);
        return ResponseEntity.ok().build();

    }

    @Operation(summary = "Sign up user",
            responses = {
                    @ApiResponse(description = "User authenticated successfully!", responseCode = "200"),
                    @ApiResponse(description = "User already exists", responseCode = "404")
            })
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid SignUpRequestDTO signUpRequestDTO) {
        userService.signup(signUpRequestDTO, null);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "Verify user",
            parameters = {
                    @Parameter(name = "token", description = "Verification token for the user", required = true)
            },
            responses = {
                    @ApiResponse(description = "User authenticated successfully!", responseCode = "200")
            }
    )
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) throws IOException {

        String viewName = userService.verify(token); // "emailVerifiedSuccess" or "emailVerifiedFailed"

        ClassPathResource resource = new ClassPathResource("templates/" + viewName + ".html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .body(html);
    }

    @Operation(summary = "Return session",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Session retrieved successfully", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JwtResponseDTO.class))),
            })
    @GetMapping("/session")
    public ResponseEntity<JwtResponseDTO> me() {
        User user = securityContext.getCurrentUser();
        return ResponseEntity.ok(JwtResponseDTO.builder()
                .username(user.getUsername())
                .pfp(user.getPfp())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map((role) -> role.getRole().name()).toList())
                .build());
    }


    @Operation(summary = "retrieve users",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Users retrieved Successfully!", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class)))
            })
    @GetMapping("/organization/users")
    @RequiredRole({OrganizationRoles.OWNER, OrganizationRoles.MEMBER})
    public ResponseEntity<PagedResponse<UserViewDTO>> getOrganizationUsers(@RequestParam @NotBlank String organizationUUID,
                                                                           Pageable pageable,
                                                                           UserFilterDTO filter) {
        return ResponseEntity.ok(userService.getOrganizationUsers(organizationUUID, pageable, filter));
    }

    @Operation(summary = "retrieve project users",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Users retrieved Successfully!", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class)))
            })
    @GetMapping("/project/users")
    @RequiredRole({OrganizationRoles.OWNER, OrganizationRoles.MEMBER})
    public ResponseEntity<List<UserViewDTO>> getProjectUsers(@RequestParam @NotBlank(message = "organizationUUID cannot be blank") String organizationUUID,
                                                             @RequestParam @NotBlank String projectUUID) {
        return ResponseEntity.ok(userService.getProjectUsers(organizationUUID, projectUUID));
    }


    @Operation(summary = "retrieve organizations",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Organizations retrieved Successfully!!", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrganizationViewResponseDTO.class)))
            })
    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationViewResponseDTO>> getUserOrganizations() {
        return ResponseEntity.ok(userService.getUserOrganizations());
    }

    @PostMapping("/login/google")
    public ResponseEntity<JwtResponseDTO> loginWithGoogle(@RequestBody Map<String, String> requestBody) {
        String googleToken = requestBody.get("token");

        if (googleToken == null || googleToken.isEmpty()) {
            throw new ApiRequestException("Google token is required");
        }

        JwtResponseDTO jwtResponse = userService.loginWithGoogle(googleToken);
        return ResponseEntity.ok(jwtResponse);
    }

}
