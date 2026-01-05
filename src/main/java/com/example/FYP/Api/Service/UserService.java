package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Organization;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Entity.UserRole;
import com.example.FYP.Api.Entity.VerificationToken;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Exception.NotVerifiedException;
import com.example.FYP.Api.Exception.UserAlreadyExistException;
import com.example.FYP.Api.Exception.UserNotFoundException;
import com.example.FYP.Api.Mapper.UserMapper;
import com.example.FYP.Api.Messaging.Model.EmailVerificationMessage;
import com.example.FYP.Api.Messaging.RabbitMqProducer;
import com.example.FYP.Api.Model.Constant.Role;
import com.example.FYP.Api.Model.Filter.UserFilterDTO;
import com.example.FYP.Api.Model.Request.LoginRequestDTO;
import com.example.FYP.Api.Model.Request.SignUpRequestDTO;
import com.example.FYP.Api.Model.Response.JwtResponseDTO;
import com.example.FYP.Api.Model.View.CommunityViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Repository.OrganizationRepository;
import com.example.FYP.Api.Repository.RoleRepository;
import com.example.FYP.Api.Repository.UserRepository;
import com.example.FYP.Api.Repository.VerificationTokenRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Specification.GenericSpecification;
import com.example.FYP.Api.Util.PagedResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityContext securityContext;
    private final RabbitMqProducer rabbitMqProducer;
    private final VerificationTokenRepository verificationTokenRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserMapper userMapper;
    private final ModelMapper modelMapper;

    public JwtResponseDTO signin(LoginRequestDTO loginRequestDTO) {
        String email = loginRequestDTO.getEmail().toLowerCase();
        if (!userRepository.existsByEmail(email))
            throw new UserNotFoundException("User not found");
        try {

            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, loginRequestDTO.getPassword()));

            if (authentication.isAuthenticated()) {
                log.info("User authenticated successfully");

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("user not found with email: " + email));

                if (!user.isVerified()) throw new NotVerifiedException("user not verified");

                return JwtResponseDTO.builder()
                        .accessToken(jwtService.GenerateToken(email))
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .roles(user.getRoles().stream().map((role) -> role.getRole().name()).toList())
                        .pfp(user.getPfp()).build();
            } else {
                log.warn("Authentication failed");
                throw new UsernameNotFoundException("email not found!");
            }
        } catch (Exception e) {
            log.error("Authentication process failed", e);
            throw e;
        }
    }

    public void createUser(String username, String email, String password, Role role) {
        email = email.toLowerCase();
        String encodedPassword = passwordEncoder.encode(password);


        Set<UserRole> roles = new HashSet<>();
        roles.add(roleRepository.findByRole(role).orElseThrow(() -> new EntityNotFoundException("role not found: " + role.name())));


        User user = userRepository.save(User.builder().email(email)
                .password(encodedPassword)
                .username(username)
                .pfp("/dummy/url")
                .roles(roles)
                .isVerified(true)
                .communityRoles(new HashSet<>())
                .build());


        userRepository.save(user);

    }

    public void createUser(String username, String email, String password) {
     createUser(username, email, password, Role.USER);
    }

    @Transactional
    public void signup(SignUpRequestDTO signUpRequestDTO, MultipartFile profilePic) {
        String email = signUpRequestDTO.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email))
            throw new UserAlreadyExistException("User already exists with this email!");

        Set<UserRole> roles = new HashSet<>();
        roles.add(roleRepository.findByRole(Role.USER)
                .orElseThrow(() -> new EntityNotFoundException("role not found: " + Role.USER.name())));
        String encodedPassword = passwordEncoder.encode(signUpRequestDTO.getPassword());

        signUpRequestDTO.setPassword(encodedPassword);

        String country = signUpRequestDTO.getCountry();
        System.out.println("🌍 Signup - Country received: " + country);
        
        User user = userRepository.save(User.builder().email(email)
                .password(encodedPassword)
                .username(signUpRequestDTO.getUsername())
                .pfp("/dummy/url")
                .country(country) // Save country from signup
                .roles(roles)
                .isVerified(true) // Auto-verify for development
                .communityRoles(new HashSet<>())
                .build());
        
        System.out.println("🌍 Signup - Country saved to user: " + user.getCountry());

        VerificationToken token = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(token);

        // Try to send verification email, but don't fail signup if it doesn't work
        try {
            EmailVerificationMessage message = EmailVerificationMessage.builder()
                    .email(user.getEmail())
                    .token(token.getToken())
                    .build();

            rabbitMqProducer.sendVerification("verificationQueue", message);
        } catch (Exception e) {
            // Log the error but allow signup to succeed
            System.err.println("Warning: Failed to send verification email - " + e.getMessage());
            // User is already auto-verified above, so they can still login
        }
    }

    public JwtResponseDTO loginWithGoogle(String googleToken) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(googleToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String username = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                Optional<User> userOptional = userRepository.findByEmail(email);
                User user = userOptional.orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .username(username)
                            .pfp(pictureUrl)
                            .isVerified(true)
                            .roles(Collections.singleton(roleRepository.findById(1L)
                                    .orElseThrow(() -> new EntityNotFoundException("Role not found"))))
                            .build();
                    return userRepository.save(newUser);
                });

                return JwtResponseDTO.builder()
                        .accessToken(jwtService.GenerateToken(email))
                        .email(user.getEmail())
                        .roles(user.getRoles().stream().map((role) -> role.getRole().name()).toList())
                        .username(user.getUsername())
                        .pfp(user.getPfp())
                        .build();
            } else {
                throw new ApiRequestException("Invalid Google ID token");
            }
        } catch (Exception e) {
            throw new ApiRequestException("Google authentication failed");
        }
    }

    public PagedResponse<UserViewDTO> getOrganizationUsers(String organizationUUID, Pageable pageable, UserFilterDTO filter) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Specification<User> specification = Specification.where(
                GenericSpecification.<User>filterByFields(filter)
        ).and((root, query, criteriaBuilder) -> {
            Join<User, Organization> organizationJoin = root.join("organizations");
            return criteriaBuilder.equal(organizationJoin.get("id"), organization.getId());
        });


        Page<UserViewDTO> userPage = userRepository.findAll(specification, pageable).map((e) -> userMapper.toUserViewDTO(e, organization.getId()));

        return PagedResponse.fromPage(userPage);
    }


    public void hasRole(String organizationUUID, List<String> roleNames) {
        User user = securityContext.getCurrentUser();

        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        boolean result = user.getCommunityRoles().stream()
                .filter(role -> role.getCommunity().getId() == organization.getId())
                .anyMatch(role -> roleNames.contains(role.getRole().name()));

        System.out.println("result : " + result);
        if (!result) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public String verify(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return "emailVerifiedFailed"; // token not found
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "emailVerifiedFailed"; // token expired
        }

        User user = verificationToken.getUser();
        if (user.isVerified()) {
            return "emailAlreadyVerified"; // optional: another view
        }

        user.setVerified(true);
        userRepository.save(user);

        return "emailVerifiedSuccess";
    }

    public List<CommunityViewDTO> getCommunities() {
        return securityContext.getCurrentUser()
                .getCommunities()
                .stream()
                .map((e) -> modelMapper.map(e, CommunityViewDTO.class))
                .collect(Collectors.toList());
    }
}