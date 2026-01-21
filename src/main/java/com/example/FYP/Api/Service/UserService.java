package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Address;
import com.example.FYP.Api.Entity.BetStatus;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Entity.UserRole;
import com.example.FYP.Api.Entity.VerificationToken;
import com.example.FYP.Api.Exception.NotVerifiedException;
import com.example.FYP.Api.Exception.UserAlreadyExistException;
import com.example.FYP.Api.Exception.UserNotFoundException;
import com.example.FYP.Api.Mapper.UserMapper;
import com.example.FYP.Api.Messaging.Model.EmailVerificationMessage;
import com.example.FYP.Api.Messaging.RabbitMqProducer;
import com.example.FYP.Api.Model.Constant.Role;
import com.example.FYP.Api.Model.Request.LoginRequestDTO;
import com.example.FYP.Api.Model.Request.SignUpRequestDTO;
import com.example.FYP.Api.Model.Response.JwtResponseDTO;
import com.example.FYP.Api.Model.View.CommunityViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Repository.BetRepository;
import com.example.FYP.Api.Repository.RoleRepository;
import com.example.FYP.Api.Repository.UserRepository;
import com.example.FYP.Api.Repository.VerificationTokenRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Util.PagedResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final BetRepository betRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final SecurityContext securityContext;
    private final RabbitMqProducer rabbitMqProducer;
    private final VerificationTokenRepository verificationTokenRepository;
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
        
        User.UserBuilder userBuilder = User.builder()
                .email(email)
                .password(encodedPassword)
                .username(signUpRequestDTO.getUsername())
                .pfp("/dummy/url")
                .roles(roles)
                .isVerified(true)
                .communityRoles(new HashSet<>());
        
        if (country != null && !country.trim().isEmpty()) {
            Address address = new Address();
            address.setCountry(country);
            userBuilder.address(address);
            System.out.println("🌍 Signup - Creating Address with country: " + country);
        }
        
        User user = userRepository.save(userBuilder.build());
        
        System.out.println("🌍 Signup - Country saved to Address: " + (user.getAddress() != null ? user.getAddress().getCountry() : "null"));

        VerificationToken token = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(token);

        try {
            EmailVerificationMessage message = EmailVerificationMessage.builder()
                    .email(user.getEmail())
                    .token(token.getToken())
                    .build();

            rabbitMqProducer.sendVerification("verificationQueue", message);
        } catch (Exception e) {
            System.err.println("Warning: Failed to send verification email - " + e.getMessage());
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

    /**
     * Get all users with optional search filter
     * @param search Optional search term for username (case-insensitive)
     * @param pageable Pagination parameters
     * @return Paged response of users
     */
    public PagedResponse<UserViewDTO> getAllUsers(String search, Pageable pageable) {
        Page<User> userPage;
        
        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.findByUsernameContainingIgnoreCase(search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        Page<UserViewDTO> dtoPage = userPage.map(this::mapUserToViewDTO);
        return PagedResponse.fromPage(dtoPage);
    }

    /**
     * Get top betters sorted by number of won bets
     * @param pageable Pagination parameters
     * @return Paged response of users sorted by wins
     */
    public PagedResponse<UserViewDTO> getTopBetters(Pageable pageable) {
        Page<User> userPage = userRepository.findTopBetters(BetStatus.WON.name(), pageable);
        Page<UserViewDTO> dtoPage = userPage.map(this::mapUserToViewDTO);
        return PagedResponse.fromPage(dtoPage);
    }

    /**
     * Get top users sorted by total points
     * @param pageable Pagination parameters
     * @return Paged response of users sorted by points (descending)
     */
    public PagedResponse<UserViewDTO> getTopPoints(Pageable pageable) {
        Page<User> userPage = userRepository.findAllByOrderByPointsDesc(pageable);
        Page<UserViewDTO> dtoPage = userPage.map(this::mapUserToViewDTO);
        return PagedResponse.fromPage(dtoPage);
    }

    /**
     * Helper method to map User entity to UserViewDTO with bet statistics
     * @param user User entity
     * @return UserViewDTO with populated statistics
     */
    private UserViewDTO mapUserToViewDTO(User user) {
        long distinctTickets = betRepository.countDistinctTicketsByUserId(user.getId());
        long nullTicketBets = betRepository.findByUserId(user.getId()).stream()
                .filter(bet -> bet.getTicketId() == null || bet.getTicketId().isEmpty())
                .count();
        long totalBets = distinctTickets + nullTicketBets;
        
        long distinctWonTickets = betRepository.countDistinctTicketsByUserIdAndStatus(user.getId(), BetStatus.WON);
        long nullTicketWonBets = betRepository.findByUserId(user.getId()).stream()
                .filter(bet -> (bet.getTicketId() == null || bet.getTicketId().isEmpty()) && bet.getStatus() == BetStatus.WON)
                .count();
        long totalWins = distinctWonTickets + nullTicketWonBets;
        
        long distinctLostTickets = betRepository.countDistinctTicketsByUserIdAndStatus(user.getId(), BetStatus.LOST);
        long nullTicketLostBets = betRepository.findByUserId(user.getId()).stream()
                .filter(bet -> (bet.getTicketId() == null || bet.getTicketId().isEmpty()) && bet.getStatus() == BetStatus.LOST)
                .count();
        long totalLost = distinctLostTickets + nullTicketLostBets;
        
        long resolvedBets = totalWins + totalLost;
        double winRate = resolvedBets > 0 ? (double) totalWins / resolvedBets * 100.0 : 0.0;
        
        UserViewDTO dto = new UserViewDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPfp(user.getPfp());
        dto.setTotalPoints(user.getPoints() != null ? user.getPoints() : 0L);
        dto.setTotalBets(totalBets);
        dto.setTotalWins(totalWins);
        dto.setTotalLost(totalLost);
        dto.setWinRate(winRate);
        dto.setAbout(user.getAbout());
        
        if (user.getAddress() != null && user.getAddress().getCountry() != null) {
            dto.setCountry(user.getAddress().getCountry());
        }
        
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> role.getRole().name())
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}