package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.*;
import com.example.AzureTestProject.Api.Exception.ApiRequestException;
import com.example.AzureTestProject.Api.Exception.NotVerifiedException;
import com.example.AzureTestProject.Api.Exception.UserAlreadyExistException;
import com.example.AzureTestProject.Api.Exception.UserNotFoundException;
import com.example.AzureTestProject.Api.Mapper.UserMapper;
import com.example.AzureTestProject.Api.Messaging.Model.EmailVerificationMessage;
import com.example.AzureTestProject.Api.Messaging.RabbitMqProducer;
import com.example.AzureTestProject.Api.Model.Constant.Role;
import com.example.AzureTestProject.Api.Model.Filter.UserFilterDTO;
import com.example.AzureTestProject.Api.Model.Request.LoginRequestDTO;
import com.example.AzureTestProject.Api.Model.Request.SignUpRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.JwtResponseDTO;
import com.example.AzureTestProject.Api.Model.Response.OrganizationViewResponseDTO;
import com.example.AzureTestProject.Api.Model.View.UserViewDTO;
import com.example.AzureTestProject.Api.Repository.*;
import com.example.AzureTestProject.Api.Security.SecurityContext;
import com.example.AzureTestProject.Api.Specification.GenericSpecification;
import com.example.AzureTestProject.Api.Util.PagedResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProjectRepository projectRepository;
    private final RabbitMqProducer rabbitMqProducer;
    private final VerificationTokenRepository verificationTokenRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserMapper userMapper;

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
        roles.add(roleRepository.findByRole(Role.USER).orElseThrow(() -> new EntityNotFoundException("role not found: " + Role.USER.name())));


        User user = userRepository.save(User.builder().email(email)
                .password(encodedPassword)
                .username(username)
                .pfp("/dummy/url")
                .roles(roles)
                .isVerified(true)
                .organizationRoles(new HashSet<>())
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

        User user = userRepository.save(User.builder().email(email)
                .password(encodedPassword)
                .username(signUpRequestDTO.getUsername())
                .pfp("/dummy/url")
                .roles(roles)
                .organizationRoles(new HashSet<>())
                .build());

        VerificationToken token = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(token);


        EmailVerificationMessage message = EmailVerificationMessage.builder()
                .email(user.getEmail())
                .token(token.getToken())
                .build();

        rabbitMqProducer.sendVerification("verificationQueue", message);
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

    public List<UserViewDTO> getProjectUsers(String organizationUUID, String projectUUID) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Project project = projectRepository.findByOrganization_UuidAndUuid(organizationUUID, projectUUID)
                .orElseThrow(() -> new EntityNotFoundException("project not found"));

        return userRepository.findAllUsersByProjectId(project.getId())
                .stream()
                .map((user) -> userMapper.toUserViewDTO(user, organization.getId()))
                .collect(Collectors.toList());
    }

    public List<OrganizationViewResponseDTO> getUserOrganizations() {
        return organizationRepository.getOrganizationViewsByUser(securityContext.getCurrentUser().getEmail());
    }

    public void hasRole(String organizationUUID, List<String> roleNames) {
        User user = securityContext.getCurrentUser();

        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        for (OrganizationRole role : user.getOrganizationRoles()) {
            System.out.println(role.getRole().name());
        }
        for (String role : roleNames) {
            System.out.println(role);
        }

        boolean result = user.getOrganizationRoles().stream()
                .filter(role -> role.getOrganization().getId() == organization.getId())
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

}