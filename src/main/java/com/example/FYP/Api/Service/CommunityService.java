package com.example.FYP.Api.Service;

import com.example.FYP.Api.Entity.Community;
import com.example.FYP.Api.Entity.CommunityRole;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Exception.ApiRequestException;
import com.example.FYP.Api.Exception.UserNotFoundException;
import com.example.FYP.Api.Model.Constant.CommunityRoles;
import com.example.FYP.Api.Model.Filter.CommunityFilterDTO;
import com.example.FYP.Api.Model.Patch.CommunityPatchDTO;
import com.example.FYP.Api.Model.Request.CommunityRequestDTO;
import com.example.FYP.Api.Model.Response.CommunityResponseDTO;
import com.example.FYP.Api.Model.Response.CommunityMemberDTO;
import com.example.FYP.Api.Model.View.CommunityViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Repository.CommunityRepository;
import com.example.FYP.Api.Repository.CommunityRoleRepository;
import com.example.FYP.Api.Repository.UserRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Util.PagedResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final SecurityContext securityContext;
    private final ModelMapper modelMapper;
    private final CommunityRoleRepository roleRepository;

    @Transactional
    public CommunityResponseDTO create(@Valid CommunityRequestDTO communityDTO, MultipartFile file, HttpServletRequest request) {
        if (communityRepository.existsByName(communityDTO.getName())) {
            throw ApiRequestException.badRequest("A community with this name already exists");
        }

        String description;
        if (communityDTO.getDescription() != null && !communityDTO.getDescription().trim().isEmpty()) {
            if (communityDTO.getShortDescription() != null && !communityDTO.getShortDescription().trim().isEmpty()) {
                description = communityDTO.getShortDescription().trim() + "\n\n" + communityDTO.getDescription().trim();
            } else {
                description = communityDTO.getDescription().trim();
            }
        } else if (communityDTO.getAbout() != null && !communityDTO.getAbout().trim().isEmpty()) {
            description = communityDTO.getAbout().trim();
        } else {
            throw ApiRequestException.badRequest("Community description is required");
        }

        String logoUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                logoUrl = saveCommunityLogo(file, request);
                log.info("Community logo uploaded: {}", logoUrl);
            } catch (IOException e) {
                log.error("Error saving community logo: {}", e.getMessage());
                throw ApiRequestException.badRequest("Failed to save community logo: " + e.getMessage());
            }
        } else if (communityDTO.getLogo() != null && !communityDTO.getLogo().isEmpty()) {
            logoUrl = communityDTO.getLogo();
        }

        String inviteCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        while (communityRepository.findByInviteCode(inviteCode).isPresent()) {
            inviteCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        User user = securityContext.getCurrentUser();

        Community community = Community.builder()
                .name(communityDTO.getName())
                .logo(logoUrl)
                .location(communityDTO.getLocation())
                .about(description)
                .inviteCode(inviteCode)
                .users(new ArrayList<>(List.of(user)))
                .rules(communityDTO.getRules())
                .creator(user)
                .build();
        Community savedCommunity = communityRepository.save(community);
        log.info("Community '{}' saved with ID: {}", savedCommunity.getName(), savedCommunity.getId());

        CommunityRole ownerRole = CommunityRole.builder()
                .role(CommunityRoles.OWNER)
                .community(savedCommunity)
                .build();
        
        CommunityRole memberRole = CommunityRole.builder()
                .role(CommunityRoles.MEMBER)
                .community(savedCommunity)
                .build();

        CommunityRole savedOwnerRole = roleRepository.save(ownerRole);
        CommunityRole savedMemberRole = roleRepository.save(memberRole);
        log.info("Community roles created: OWNER (ID: {}), MEMBER (ID: {})",
                savedOwnerRole.getId(), savedMemberRole.getId());

        savedCommunity.setRoles(new HashSet<>(List.of(savedOwnerRole, savedMemberRole)));
        user.getCommunityRoles().addAll(new HashSet<>(List.of(savedOwnerRole, savedMemberRole)));

        communityRepository.save(savedCommunity);
        userRepository.save(user);
        
        log.info("Community '{}' created by user {} (ID: {})",
                savedCommunity.getName(), user.getUsername(), user.getId());
        return mapToDTO(savedCommunity);
    }

    private String saveCommunityLogo(MultipartFile file, HttpServletRequest request) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        String uploadDir = "uploads/communities";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        String fileExtension = "";
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        } else {
            if (contentType != null) {
                if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                    fileExtension = ".jpg";
                } else if (contentType.contains("png")) {
                    fileExtension = ".png";
                } else if (contentType.contains("gif")) {
                    fileExtension = ".gif";
                } else if (contentType.contains("webp")) {
                    fileExtension = ".webp";
                } else {
                    fileExtension = ".jpg";
                }
            } else {
                fileExtension = ".jpg";
            }
        }

        String fileName = "community_" + System.currentTimeMillis() + "_" + 
                (file.getOriginalFilename() != null ? file.getOriginalFilename().hashCode() : "logo") + fileExtension;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File saved to: {}", filePath.toAbsolutePath());

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String baseUrl = scheme + "://" + serverName +
                (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + contextPath;
        String logoUrl = baseUrl + "/uploads/communities/" + fileName;

        return logoUrl;
    }

    public void revokeRole(Long communityId, String email, List<CommunityRoles> roles) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        User userInfo = userRepository.findByEmailAndCommunityId(email, community.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<CommunityRole> rolesToRevoke = roleRepository.findByRoleIn(roles);

        if (rolesToRevoke.isEmpty()) {
            throw new EntityNotFoundException("Roles not found");
        }

        rolesToRevoke.forEach(role -> {
            userInfo.getCommunityRoles().remove(role);
        });

        userRepository.save(userInfo);
    }

    public void assignRole(Long communityId, String email, List<CommunityRoles> roles) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        User userInfo = userRepository.findByEmailAndCommunityId(email, community.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<CommunityRole> rolesToAssign = roleRepository.findByRoleIn(roles);

        if (rolesToAssign.isEmpty()) {
            throw new EntityNotFoundException("Roles not found");
        }

        rolesToAssign.forEach(role -> {
            if (!userInfo.getRoles().contains(role)) {
                userInfo.getCommunityRoles().add(role);
            }
        });

        userRepository.save(userInfo);
    }

    @Transactional
    public CommunityViewDTO get(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
        
        if (community.getInviteCode() == null || community.getInviteCode().isEmpty()) {
            String newCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            while (communityRepository.findByInviteCode(newCode).isPresent()) {
                newCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }
            
            community.setInviteCode(newCode);
            communityRepository.save(community);
            
            log.info("Self-healed community {} with new invite code: {}", communityId, newCode);
        }
        
        return mapToDTOView(community);
    }

    @Transactional
    public void patch(Long communityId, @Valid CommunityPatchDTO communityPatchDTO) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));

        if (communityPatchDTO.getName() != null) community.setName(communityPatchDTO.getName());
        if (communityPatchDTO.getLogo() != null) community.setLogo(communityPatchDTO.getLogo());
        if (communityPatchDTO.getLocation() != null) community.setLocation(communityPatchDTO.getLocation());
        if (communityPatchDTO.getAbout() != null) community.setAbout(communityPatchDTO.getAbout());
        if (communityPatchDTO.getRules() != null) community.setRules(communityPatchDTO.getRules());

        communityRepository.save(community);
    }

    public PagedResponse<CommunityResponseDTO> getAll(Pageable pageable, CommunityFilterDTO filter) {
        Page<CommunityResponseDTO> page = communityRepository.findAll(pageable)
                .map(this::mapToDTO);
        return PagedResponse.fromPage(page);
    }

    public void delete(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
        communityRepository.delete(community);
    }

    public List<UserViewDTO> leaderboard(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        return community.getUsers().stream()
                .sorted((u1, u2) -> u2.getPoints().compareTo(u1.getPoints()))
                .map(user -> modelMapper.map(user, UserViewDTO.class))
                .toList();
    }

    public List<UserViewDTO> getMembers(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        return community.getUsers().stream()
                .map(user -> modelMapper.map(user, UserViewDTO.class))
                .toList();
    }

    public List<CommunityMemberDTO> getMembersWithRoles(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        return community.getUsers().stream()
                .map(user -> {
                    List<String> communityRoles = user.getCommunityRoles().stream()
                            .filter(role -> role.getCommunity().getId().equals(communityId))
                            .map(role -> role.getRole().name())
                            .toList();

                    return CommunityMemberDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .pfp(user.getPfp())
                            .points(user.getPoints())
                            .about(user.getAbout())
                            .country(user.getAddress() != null ? user.getAddress().getCountry() : null)
                            .roles(communityRoles)
                            .build();
                })
                .toList();
    }

    public List<CommunityMemberDTO> getModerators(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        List<CommunityRole> moderatorRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.MODERATOR);
        
        if (moderatorRoles.isEmpty()) {
            return List.of();
        }

        CommunityRole moderatorRole = moderatorRoles.get(0);

        return moderatorRole.getUsers().stream()
                .map(user -> {
                    List<String> communityRoles = user.getCommunityRoles().stream()
                            .filter(role -> role.getCommunity().getId().equals(communityId))
                            .map(role -> role.getRole().name())
                            .toList();

                    return CommunityMemberDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .pfp(user.getPfp())
                            .points(user.getPoints())
                            .about(user.getAbout())
                            .country(user.getAddress() != null ? user.getAddress().getCountry() : null)
                            .roles(communityRoles)
                            .build();
                })
                .toList();
    }

    @Transactional
    public void leave(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
        User user = securityContext.getCurrentUser();
        community.getUsers().remove(user);
        communityRepository.save(community);
    }

    @Transactional
    public void join(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
        User user = securityContext.getCurrentUser();

        if (!community.getUsers().contains(user)) {
            community.getUsers().add(user);
            communityRepository.save(community);
        }else{
            throw ApiRequestException.badRequest("user already in this community");
        }
    }

    @Transactional
    public void joinByInviteCode(String inviteCode) {
        Community community = communityRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new EntityNotFoundException("Invalid invite code"));
        
        User user = securityContext.getCurrentUser();

        if (!community.getUsers().contains(user)) {
            community.getUsers().add(user);
            communityRepository.save(community);
        } else {
            throw ApiRequestException.badRequest("user already in this community");
        }
    }

    @Transactional
    public void joinCommunity(String inviteCode, User user) {
        Community community = communityRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new EntityNotFoundException("Community not found with the provided invite code"));
        
        if (community.getUsers().contains(user)) {
            throw ApiRequestException.badRequest("Already a member");
        }
        
        community.getUsers().add(user);
        
        communityRepository.save(community);
    }

    public List<CommunityResponseDTO> getJoinedCommunities(Long userId) {
        List<Community> communities = communityRepository.findByUsers_Id(userId);
        return communities.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private UserViewDTO mapUserToDTO(User user, Long communityId) {
        UserViewDTO dto = modelMapper.map(user, UserViewDTO.class);
        
        dto.setId(user.getId());
        
        if (dto.getTotalPoints() == null && user.getPoints() != null) {
            dto.setTotalPoints(user.getPoints());
        }
        
        List<String> communityRoles = user.getCommunityRoles().stream()
                .filter(role -> role.getCommunity() != null && role.getCommunity().getId().equals(communityId))
                .map(role -> role.getRole().name())
                .distinct()
                .toList();
        dto.setRoles(communityRoles);
        
        if (user.getAddress() != null && user.getAddress().getCountry() != null) {
            dto.setCountry(user.getAddress().getCountry());
        }
        
        return dto;
    }

    private List<UserViewDTO> getModeratorsForCommunity(Community community) {
        Long communityId = community.getId();
        
        List<User> moderatorUsers = community.getUsers().stream()
                .filter(user -> {
                    return user.getCommunityRoles().stream()
                            .anyMatch(role -> {
                                if (role.getCommunity() == null) return false;
                                if (!role.getCommunity().getId().equals(communityId)) return false;
                                CommunityRoles roleType = role.getRole();
                                return roleType == CommunityRoles.OWNER || 
                                       roleType == CommunityRoles.MODERATOR;
                            });
                })
                .distinct()
                .toList();
        
        List<UserViewDTO> moderators = moderatorUsers.stream()
                .map(user -> mapUserToDTO(user, communityId))
                .toList();
        
        boolean hasOwner = moderators.stream().anyMatch(m -> m.getRoles() != null && m.getRoles().contains("OWNER"));
        if (!hasOwner && !community.getUsers().isEmpty()) {
            log.warn("No OWNER found for community {} (ID: {}). Creator might not have OWNER role assigned.",
                    community.getName(), communityId);
            List<CommunityRole> ownerRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.OWNER);
            if (!ownerRoles.isEmpty()) {
                for (User user : community.getUsers()) {
                    boolean hasOwnerRole = user.getCommunityRoles().stream()
                            .anyMatch(role -> {
                                if (role.getCommunity() == null) return false;
                                if (!role.getCommunity().getId().equals(communityId)) return false;
                                return role.getRole() == CommunityRoles.OWNER;
                            });
                    if (hasOwnerRole && moderators.stream().noneMatch(m -> m.getId() != null && m.getId() == user.getId())) {
                        moderators.add(0, mapUserToDTO(user, communityId));
                        log.info("Added missing OWNER to moderators list for community {}", communityId);
                    }
                }
            }
        }
        
        return moderators;
    }

    private List<UserViewDTO> getLeaderboardForCommunity(Community community) {
        Long communityId = community.getId();
        
        return community.getUsers().stream()
                .filter(user -> user.getPoints() != null)
                .sorted((u1, u2) -> {
                    Long p1 = u1.getPoints() != null ? u1.getPoints() : 0L;
                    Long p2 = u2.getPoints() != null ? u2.getPoints() : 0L;
                    return p2.compareTo(p1);
                })
                .limit(3)
                .map(user -> mapUserToDTO(user, communityId))
                .toList();
    }

    private CommunityResponseDTO mapToDTO(Community community) {
        List<CommunityRole> moderatorRoles = roleRepository.findByCommunityIdAndRole(community.getId(), CommunityRoles.MODERATOR);
        List<Long> moderatorIds = moderatorRoles.isEmpty() 
                ? List.of() 
                : moderatorRoles.get(0).getUsers().stream()
                        .map(User::getId)
                        .toList();

        List<UserViewDTO> moderators = getModeratorsForCommunity(community);
        List<UserViewDTO> leaderboard = getLeaderboardForCommunity(community);

        return CommunityResponseDTO.builder()
                .id(community.getId())
                .name(community.getName())
                .logo(community.getLogo())
                .location(community.getLocation())
                .about(community.getAbout())
                .rules(community.getRules())
                .inviteCode(community.getInviteCode())
                .userIds(community.getUsers().stream().map(User::getId).toList())
                .moderatorIds(moderatorIds)
                .moderators(moderators)
                .leaderboard(leaderboard)
                .build();
    }

    private CommunityViewDTO mapToDTOView(Community community) {
        List<UserViewDTO> moderators = getModeratorsForCommunity(community);
        List<UserViewDTO> leaderboard = getLeaderboardForCommunity(community);

        return CommunityViewDTO.builder()
                .id(community.getId())
                .name(community.getName())
                .logo(community.getLogo())
                .location(community.getLocation())
                .about(community.getAbout())
                .rules(community.getRules())
                .inviteCode(community.getInviteCode())
                .userIds(community.getUsers().stream().map(User::getId).toList())
                .moderators(moderators)
                .leaderboard(leaderboard)
                .build();
    }

    public void kick(Long communityId, String email) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        User user = userRepository.findByEmailAndCommunityId(email, communityId)
                .orElseThrow(() -> new EntityNotFoundException("user not part of community"));

        community.getUsers().remove(user);
        communityRepository.save(community);
    }

    public List<String> getRoles(Long communityId, String email) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        User user = userRepository.findByEmailAndCommunityId(email, communityId)
                .orElseThrow(() -> new EntityNotFoundException("user not part of community"));


            return roleRepository.findRolesByUserAndCommunity(email, communityId);
        }

    @Transactional
    public void promoteToModerator(Long communityId, Long userId, User requester) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (community.getCreator() == null) {
            throw ApiRequestException.badRequest("Community has no creator. Cannot determine permissions.");
        }
        
        if (community.getCreator().getId() != requester.getId()) {
            throw ApiRequestException.badRequest("Only community OWNER (creator) can promote members");
        }

        List<CommunityRole> existingModeratorRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.MODERATOR);
        CommunityRole moderatorRole;
        
        if (existingModeratorRoles.isEmpty()) {
            moderatorRole = CommunityRole.builder()
                    .role(CommunityRoles.MODERATOR)
                    .community(community)
                    .build();
            moderatorRole = roleRepository.save(moderatorRole);
            log.info("📝 Created new MODERATOR role for community {}", communityId);
        } else {
            moderatorRole = existingModeratorRoles.get(0);
        }

        boolean alreadyHasModeratorRole = user.getCommunityRoles().stream()
                .anyMatch(role -> role.getCommunity() != null 
                        && role.getCommunity().getId().equals(communityId)
                        && role.getRole() == CommunityRoles.MODERATOR);
        
        if (!alreadyHasModeratorRole) {
            user.getCommunityRoles().add(moderatorRole);
            userRepository.save(user);
            log.info("User {} promoted to MODERATOR in community {}. Role assigned via JPA.", userId, communityId);
        } else {
            log.info("User {} already has MODERATOR role in community {}", userId, communityId);
        }
    }

    @Transactional
    public void demoteToMember(Long communityId, Long userId, User requester) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (community.getCreator() == null) {
            throw ApiRequestException.badRequest("Community has no creator. Cannot determine permissions.");
        }
        
        if (community.getCreator().getId() != requester.getId()) {
            throw ApiRequestException.badRequest("Only community OWNER (creator) can demote moderators");
        }

        if (community.getCreator().getId() == userId) {
            throw ApiRequestException.badRequest("Cannot demote the community OWNER (creator)");
        }

        List<CommunityRole> moderatorRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.MODERATOR);
        
        if (moderatorRoles.isEmpty()) {
            log.info("ℹ️ No MODERATOR role found for community {}. User {} may not be a moderator.", communityId, userId);
            return;
        }

        CommunityRole moderatorRole = moderatorRoles.get(0);

        boolean removed = user.getCommunityRoles().removeIf(role ->
                role.getCommunity() != null 
                && role.getCommunity().getId().equals(communityId)
                && role.getRole() == CommunityRoles.MODERATOR);
        
        if (removed) {
            userRepository.save(user);
            log.info("User {} demoted from MODERATOR to MEMBER in community {}. Role removed via JPA.", userId, communityId);
        } else {
            log.info("ℹUser {} does not have MODERATOR role in community {}", userId, communityId);
        }
    }
}
