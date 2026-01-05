package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Loader.Annotation.Feature;
import com.example.FYP.Api.Model.Filter.UserFilterDTO;
import com.example.FYP.Api.Model.Request.LoginRequestDTO;
import com.example.FYP.Api.Model.Request.SignUpRequestDTO;
import com.example.FYP.Api.Model.Response.JwtResponseDTO;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Model.View.CommunityViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.UserRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Service.UserService;
import com.example.FYP.Api.Util.PagedResponse;
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
    private final BetRepository betRepository;

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
            @RequestBody @Valid SignUpRequestDTO signUpRequestDTO) {
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
                .points(user.getPoints() != null ? user.getPoints() : 0L)
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
    public ResponseEntity<PagedResponse<UserViewDTO>> getOrganizationUsers(@RequestParam @NotBlank String organizationUUID,
                                                                           Pageable pageable,
                                                                           UserFilterDTO filter) {
        return ResponseEntity.ok(userService.getOrganizationUsers(organizationUUID, pageable, filter));
    }



    @GetMapping("/communities")
    public ResponseEntity<List<CommunityViewDTO>> getOrganizationUsers() {
        return ResponseEntity.ok(userService.getCommunities());
    }

    @Operation(summary = "Get user profile with statistics",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "User profile retrieved successfully", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserViewDTO.class)))
            })
    @GetMapping("/profile")
    public ResponseEntity<UserViewDTO> getProfile() {
        User currentUser = securityContext.getCurrentUser();
        
        // Calculate betting statistics
        long totalBets = betRepository.countByUserId(currentUser.getId());
        long totalWins = betRepository.countByUserIdAndStatus(currentUser.getId(), BetStatus.WON);
        
        // Calculate win rate (percentage)
        double winRate = totalBets > 0 ? (double) totalWins / totalBets * 100.0 : 0.0;
        
        // Build UserViewDTO with profile data
        UserViewDTO profileDTO = new UserViewDTO();
        profileDTO.setUsername(currentUser.getUsername());
        profileDTO.setEmail(currentUser.getEmail());
        profileDTO.setPfp(currentUser.getPfp());
        profileDTO.setTotalPoints(currentUser.getPoints() != null ? currentUser.getPoints() : 0L);
        profileDTO.setTotalBets(totalBets);
        profileDTO.setTotalWins(totalWins);
        profileDTO.setWinRate(winRate);
        profileDTO.setRoles(currentUser.getRoles().stream()
                .map(role -> role.getRole().name())
                .toList());
        
        return ResponseEntity.ok(profileDTO);
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
