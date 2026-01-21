package com.example.FYP.Api.Controller;

import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Loader.Annotation.Feature;
import com.example.FYP.Api.Model.Filter.UserFilterDTO;
import com.example.FYP.Api.Model.Request.LoginRequestDTO;
import com.example.FYP.Api.Model.Request.SignUpRequestDTO;
import com.example.FYP.Api.Model.Response.JwtResponseDTO;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Exception.UserNotFoundException;
import com.example.FYP.Api.Model.Request.ChangePasswordDTO;
import com.example.FYP.Api.Model.Request.UpdateAboutDTO;
import com.example.FYP.Api.Model.View.CommunityViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
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
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final PasswordEncoder passwordEncoder;

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
        
        long distinctTickets = betRepository.countDistinctTicketsByUserId(currentUser.getId());
        long nullTicketBets = betRepository.findByUserId(currentUser.getId()).stream()
                .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                .count();
        long totalBets = distinctTickets + nullTicketBets;
        
        long distinctWonTickets = betRepository.countDistinctTicketsByUserIdAndStatus(currentUser.getId(), BetStatus.WON);
        long nullTicketWonBets = betRepository.findByUserId(currentUser.getId()).stream()
                .filter(bet -> (bet.getTicketId() == null || bet.getTicketId().isEmpty()) && bet.getStatus() == BetStatus.WON)
                .count();
        long totalWins = distinctWonTickets + nullTicketWonBets;
        
        long distinctLostTickets = betRepository.countDistinctTicketsByUserIdAndStatus(currentUser.getId(), BetStatus.LOST);
        long nullTicketLostBets = betRepository.findByUserId(currentUser.getId()).stream()
                .filter(bet -> (bet.getTicketId() == null || bet.getTicketId().isEmpty()) && bet.getStatus() == BetStatus.LOST)
                .count();
        long totalLost = distinctLostTickets + nullTicketLostBets;
        
        long resolvedBets = totalWins + totalLost;
        double winRate = resolvedBets > 0 ? (double) totalWins / resolvedBets * 100.0 : 0.0;
        
        UserViewDTO profileDTO = new UserViewDTO();
        profileDTO.setUsername(currentUser.getUsername());
        profileDTO.setEmail(currentUser.getEmail());
        profileDTO.setPfp(currentUser.getPfp());
        profileDTO.setTotalPoints(currentUser.getPoints() != null ? currentUser.getPoints() : 0L);
        profileDTO.setTotalBets(totalBets);
        profileDTO.setTotalWins(totalWins);
        profileDTO.setTotalLost(totalLost);
        profileDTO.setWinRate(winRate);
        profileDTO.setAbout(currentUser.getAbout());
        
        String country = null;
        if (currentUser.getAddress() != null && currentUser.getAddress().getCountry() != null) {
            country = currentUser.getAddress().getCountry();
            System.out.println("🌍 Profile - Country from Address: " + country);
        }
        
        profileDTO.setCountry(country); // Include country from User entity
        System.out.println("🌍 Profile - DTO country set to: " + profileDTO.getCountry());
        
        profileDTO.setRoles(currentUser.getRoles().stream()
                .map(role -> role.getRole().name())
                .toList());
        
        return ResponseEntity.ok(profileDTO);
    }

    @Operation(summary = "Update user about section",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "About section updated successfully", responseCode = "200")
            })
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateAbout(@RequestBody @Valid UpdateAboutDTO updateAboutDTO) {
        User currentUser = securityContext.getCurrentUser();
        currentUser.setAbout(updateAboutDTO.getAbout());
        userRepository.save(currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change user password",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Password changed successfully", responseCode = "200"),
                    @ApiResponse(description = "Invalid old password", responseCode = "400")
            })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        User currentUser = securityContext.getCurrentUser();
        
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), currentUser.getPassword())) {
            throw new ApiRequestException("Invalid old password");
        }
        
        String encodedNewPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        currentUser.setPassword(encodedNewPassword);
        userRepository.save(currentUser);
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Upload user avatar",
            parameters = {
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Avatar uploaded successfully", responseCode = "200")
            })
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        User currentUser = securityContext.getCurrentUser();
        
        System.out.println("📸 Avatar upload received for user: " + currentUser.getUsername());
        System.out.println("📸 File name: " + file.getOriginalFilename());
        System.out.println("📸 File size: " + file.getSize() + " bytes");
        System.out.println("📸 Content type: " + file.getContentType());
        
        if (file.isEmpty()) {
            throw new ApiRequestException("File is empty");
        }
        
        String uploadDir = "uploads/avatars";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("📁 Created upload directory: " + uploadPath.toAbsolutePath());
        }
        
        String fileExtension = "";
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        } else {
            if (file.getContentType() != null) {
                if (file.getContentType().contains("jpeg") || file.getContentType().contains("jpg")) {
                    fileExtension = ".jpg";
                } else if (file.getContentType().contains("png")) {
                    fileExtension = ".png";
                } else {
                    fileExtension = ".jpg"; // Default
                }
            } else {
                fileExtension = ".jpg";
            }
        }
        
        String fileName = "avatar_" + currentUser.getId() + "_" + System.currentTimeMillis() + fileExtension;
        Path filePath = uploadPath.resolve(fileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("💾 File saved to: " + filePath.toAbsolutePath());
        
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        String baseUrl = scheme + "://" + serverName + (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + contextPath;
        String avatarUrl = baseUrl + "/uploads/avatars/" + fileName;
        
        currentUser.setPfp(avatarUrl);
        userRepository.save(currentUser);
        
        System.out.println("✅ Avatar URL updated: " + avatarUrl);
        
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }




    @Operation(summary = "Get all users with optional search",
            parameters = {
                    @Parameter(name = "search", description = "Search term for username (optional)", required = false),
                    @Parameter(name = "page", description = "Page number (0-indexed)", required = false),
                    @Parameter(name = "size", description = "Page size", required = false),
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Users retrieved successfully", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class)))
            })
    @GetMapping
    public ResponseEntity<PagedResponse<UserViewDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(search, pageable));
    }

    @Operation(summary = "Get top betters (users sorted by number of won bets)",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", required = false),
                    @Parameter(name = "size", description = "Page size", required = false),
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Top betters retrieved successfully", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class)))
            })
    @GetMapping("/leaderboard/betters")
    public ResponseEntity<PagedResponse<UserViewDTO>> getTopBetters(Pageable pageable) {
        return ResponseEntity.ok(userService.getTopBetters(pageable));
    }

    @Operation(summary = "Get top users by points",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", required = false),
                    @Parameter(name = "size", description = "Page size", required = false),
                    @Parameter(name = "Authorization",
                            description = "Bearer token for authentication",
                            required = true,
                            in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(description = "Top users by points retrieved successfully", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class)))
            })
    @GetMapping("/leaderboard/points")
    public ResponseEntity<PagedResponse<UserViewDTO>> getTopPoints(Pageable pageable) {
        return ResponseEntity.ok(userService.getTopPoints(pageable));
    }

}
