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
        // Validation: Check if a community with this name already exists
        if (communityRepository.existsByName(communityDTO.getName())) {
            throw ApiRequestException.badRequest("A community with this name already exists");
        }

        // Map DTO fields to entity
        // Use description if provided, otherwise fall back to about
        String description = communityDTO.getDescription() != null 
                ? communityDTO.getDescription() 
                : communityDTO.getAbout();

        // Handle file upload if provided
        String logoUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                logoUrl = saveCommunityLogo(file, request);
                log.info("✅ Community logo uploaded: {}", logoUrl);
            } catch (IOException e) {
                log.error("❌ Error saving community logo: {}", e.getMessage());
                throw ApiRequestException.badRequest("Failed to save community logo: " + e.getMessage());
            }
        } else if (communityDTO.getLogo() != null && !communityDTO.getLogo().isEmpty()) {
            // Fallback to legacy logo field if no file uploaded
            logoUrl = communityDTO.getLogo();
        }

        // Generate unique invite code before creating the community
        String inviteCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Ensure the code is unique (very unlikely collision, but safety check)
        while (communityRepository.findByInviteCode(inviteCode).isPresent()) {
            inviteCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Community community = Community.builder()
                .name(communityDTO.getName())
                .logo(logoUrl)
                .location(communityDTO.getLocation())
                .about(description) // Map description to about field
                .inviteCode(inviteCode)
                .users(new ArrayList<>(List.of(securityContext.getCurrentUser())))
                .rules(communityDTO.getRules())
                .build();

        // Set createdAt is handled by AuditableEntity
        // memberCount is derived from users.size() (starts with 1 - the admin creator)

        CommunityRole role = CommunityRole.builder().role(CommunityRoles.OWNER).community(community).build();
        CommunityRole roleMember = CommunityRole.builder().role(CommunityRoles.MEMBER).community(community).build();

        User user = securityContext.getCurrentUser();

        // Set the creator of the community
        community.setCreator(user);

        user.getCommunityRoles().addAll(new HashSet<>(List.of(role, roleMember)));

        community.setRoles(new HashSet<>(List.of(role, roleMember)));

        // Save user to persist role associations
        userRepository.save(user);
        
        Community saved = communityRepository.save(community);
        log.info("✅ Community '{}' created by user {} (ID: {})", saved.getName(), user.getUsername(), user.getId());
        return mapToDTO(saved);
    }

    /**
     * Save community logo file and return the public URL
     */
    private String saveCommunityLogo(MultipartFile file, HttpServletRequest request) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type (only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Create uploads directory if it doesn't exist
        String uploadDir = "uploads/communities";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("📁 Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        // Generate unique filename
        String fileExtension = "";
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        } else {
            // Determine extension from content type
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
                    fileExtension = ".jpg"; // Default
                }
            } else {
                fileExtension = ".jpg";
            }
        }

        String fileName = "community_" + System.currentTimeMillis() + "_" + 
                (file.getOriginalFilename() != null ? file.getOriginalFilename().hashCode() : "logo") + fileExtension;
        Path filePath = uploadPath.resolve(fileName);

        // Save the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("💾 File saved to: {}", filePath.toAbsolutePath());

        // Construct the full URL from the request
        String scheme = request.getScheme(); // http or https
        String serverName = request.getServerName(); // IP or hostname
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath(); // /api/v1

        // Build the full URL
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
        
        // Self-healing: Generate invite code if missing (for old communities)
        if (community.getInviteCode() == null || community.getInviteCode().isEmpty()) {
            // Generate a unique code
            String newCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Ensure the code is unique (very unlikely collision, but safety check)
            while (communityRepository.findByInviteCode(newCode).isPresent()) {
                newCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }
            
            // Set and save the new code
            community.setInviteCode(newCode);
            communityRepository.save(community);
            
            log.info("🔧 Self-healed community {} with new invite code: {}", communityId, newCode);
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

    /**
     * Get community members with their roles
     */
    public List<CommunityMemberDTO> getMembersWithRoles(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        return community.getUsers().stream()
                .map(user -> {
                    // Get user's roles in this community
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

    /**
     * Get only moderators of the community
     */
    public List<CommunityMemberDTO> getModerators(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        // Find MODERATOR role for this community
        List<CommunityRole> moderatorRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.MODERATOR);
        
        if (moderatorRoles.isEmpty()) {
            return List.of(); // No moderators
        }

        CommunityRole moderatorRole = moderatorRoles.get(0);

        // Get all users with MODERATOR role
        return moderatorRole.getUsers().stream()
                .map(user -> {
                    // Get all user's roles in this community
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
        // Find community by invite code
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
        // Find community by invite code
        Community community = communityRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new EntityNotFoundException("Community not found with the provided invite code"));
        
        // Check if user is already a member
        if (community.getUsers().contains(user)) {
            throw ApiRequestException.badRequest("Already a member");
        }
        
        // Add user to community
        community.getUsers().add(user);
        
        // Save community (memberCount is derived from users.size(), no need to increment separately)
        communityRepository.save(community);
    }

    public List<CommunityResponseDTO> getJoinedCommunities(Long userId) {
        List<Community> communities = communityRepository.findByUsers_Id(userId);
        return communities.stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Map User entity to UserViewDTO with community-specific roles
     */
    private UserViewDTO mapUserToDTO(User user, Long communityId) {
        UserViewDTO dto = modelMapper.map(user, UserViewDTO.class);
        
        // Map ID (ModelMapper might not map it if it's @JsonIgnore)
        dto.setId(user.getId());
        
        // Map points
        if (dto.getTotalPoints() == null && user.getPoints() != null) {
            dto.setTotalPoints(user.getPoints());
        }
        
        // Get community roles for this user
        List<String> communityRoles = user.getCommunityRoles().stream()
                .filter(role -> role.getCommunity() != null && role.getCommunity().getId().equals(communityId))
                .map(role -> role.getRole().name())
                .distinct()
                .toList();
        dto.setRoles(communityRoles);
        
        // Map country from address
        if (user.getAddress() != null && user.getAddress().getCountry() != null) {
            dto.setCountry(user.getAddress().getCountry());
        }
        
        return dto;
    }

    /**
     * Get moderators (OWNER and MODERATOR roles) for a community
     * Ensures the creator (OWNER) is always included
     */
    private List<UserViewDTO> getModeratorsForCommunity(Community community) {
        Long communityId = community.getId();
        
        // Query from community's users and filter by roles
        // This is the most reliable approach as it queries from the owning side (User.communityRoles)
        List<User> moderatorUsers = community.getUsers().stream()
                .filter(user -> {
                    // Check if user has OWNER or MODERATOR role for this specific community
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
        
        // Map to UserViewDTO
        List<UserViewDTO> moderators = moderatorUsers.stream()
                .map(user -> mapUserToDTO(user, communityId))
                .toList();
        
        // Ensure creator (OWNER) is always included
        // If no OWNER found, log warning (this should not happen if community was created properly)
        boolean hasOwner = moderators.stream().anyMatch(m -> m.getRoles() != null && m.getRoles().contains("OWNER"));
        if (!hasOwner && !community.getUsers().isEmpty()) {
            log.warn("⚠️ No OWNER found for community {} (ID: {}). Creator might not have OWNER role assigned.", 
                    community.getName(), communityId);
            // Try to find and add creator by checking role repository
            List<CommunityRole> ownerRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.OWNER);
            if (!ownerRoles.isEmpty()) {
                // Find users with OWNER role by ID comparison
                for (User user : community.getUsers()) {
                    boolean hasOwnerRole = user.getCommunityRoles().stream()
                            .anyMatch(role -> {
                                if (role.getCommunity() == null) return false;
                                if (!role.getCommunity().getId().equals(communityId)) return false;
                                return role.getRole() == CommunityRoles.OWNER;
                            });
                    if (hasOwnerRole && moderators.stream().noneMatch(m -> m.getId().equals(user.getId()))) {
                        moderators.add(0, mapUserToDTO(user, communityId)); // Add OWNER at the beginning
                        log.info("✅ Added missing OWNER to moderators list for community {}", communityId);
                    }
                }
            }
        }
        
        return moderators;
    }

    /**
     * Get top 3 leaderboard members by points
     */
    private List<UserViewDTO> getLeaderboardForCommunity(Community community) {
        Long communityId = community.getId();
        
        // Get all members, sort by points descending, limit to top 3
        return community.getUsers().stream()
                .filter(user -> user.getPoints() != null) // Only users with points
                .sorted((u1, u2) -> {
                    Long p1 = u1.getPoints() != null ? u1.getPoints() : 0L;
                    Long p2 = u2.getPoints() != null ? u2.getPoints() : 0L;
                    return p2.compareTo(p1); // Descending order
                })
                .limit(3)
                .map(user -> mapUserToDTO(user, communityId))
                .toList();
    }

    private CommunityResponseDTO mapToDTO(Community community) {
        // Get moderator IDs for this community
        List<CommunityRole> moderatorRoles = roleRepository.findByCommunityIdAndRole(community.getId(), CommunityRoles.MODERATOR);
        List<Long> moderatorIds = moderatorRoles.isEmpty() 
                ? List.of() 
                : moderatorRoles.get(0).getUsers().stream()
                        .map(User::getId)
                        .toList();

        // Get moderators and leaderboard
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
        // Get moderators and leaderboard
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

    /**
     * Promote a user to MODERATOR role in a community
     * Only OWNER (creator) can promote members
     */
    @Transactional
    public void promoteToModerator(Long communityId, Long userId, User requester) {
        // Verify community exists
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        // Fix Permission Check: Ensure creator is not null before checking ID
        boolean isOwner = false;
        if (community.getCreator() != null) {
            // Check if requester is the creator
            isOwner = community.getCreator().getId().equals(requester.getId());
            log.debug("Checking creator permission: creatorId={}, requesterId={}, isOwner={}", 
                    community.getCreator().getId(), requester.getId(), isOwner);
        } else {
            // Fallback: Check CommunityRole if creator is null (for old communities)
            log.warn("⚠️ Community {} has null creator, falling back to CommunityRole check", communityId);
            isOwner = requester.getCommunityRoles().stream()
                    .anyMatch(role -> role.getCommunity().getId().equals(communityId) 
                            && role.getRole() == CommunityRoles.OWNER);
        }
        
        if (!isOwner) {
            throw ApiRequestException.badRequest("Only community OWNER (creator) can promote members");
        }

        // Find the user to promote
        User userToPromote = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Verify user is a member of the community
        if (!community.getUsers().contains(userToPromote)) {
            throw ApiRequestException.badRequest("User is not a member of this community");
        }

        // Check if user is already a moderator
        boolean isAlreadyModerator = userToPromote.getCommunityRoles().stream()
                .anyMatch(role -> role.getCommunity().getId().equals(communityId) 
                        && role.getRole() == CommunityRoles.MODERATOR);
        
        if (isAlreadyModerator) {
            throw ApiRequestException.badRequest("User is already a moderator");
        }

        // Execute Update: Use native query to update role directly in community_users table
        log.info("🔄 Updating role to MODERATOR for user {} in community {} using native query", userId, communityId);
        communityRepository.updateMemberRole(communityId, userId, "MODERATOR");
        
        // Also update CommunityRole entity for consistency (for queries that use it)
        List<CommunityRole> moderatorRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.MODERATOR);
        CommunityRole moderatorRole;
        
        if (moderatorRoles.isEmpty()) {
            // Create new MODERATOR role for this community
            moderatorRole = CommunityRole.builder()
                    .role(CommunityRoles.MODERATOR)
                    .community(community)
                    .build();
            moderatorRole = roleRepository.save(moderatorRole);
            community.getRoles().add(moderatorRole);
            communityRepository.save(community);
        } else {
            moderatorRole = moderatorRoles.get(0);
        }

        // Assign MODERATOR role to user in CommunityRole entity (for queries)
        if (!userToPromote.getCommunityRoles().contains(moderatorRole)) {
            userToPromote.getCommunityRoles().add(moderatorRole);
            userRepository.save(userToPromote);
        }
        
        log.info("✅ User {} promoted to MODERATOR in community {}. Role updated in database.", userId, communityId);
    }

    /**
     * Demote a MODERATOR back to regular MEMBER
     * Only OWNER (creator) can demote moderators
     */
    @Transactional
    public void demoteToMember(Long communityId, Long userId, User requester) {
        // Verify community exists
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        // Fix Permission Check: Ensure creator is not null before checking ID
        boolean isOwner = false;
        if (community.getCreator() != null) {
            // Check if requester is the creator
            isOwner = community.getCreator().getId().equals(requester.getId());
            log.debug("Checking creator permission for demote: creatorId={}, requesterId={}, isOwner={}", 
                    community.getCreator().getId(), requester.getId(), isOwner);
        } else {
            // Fallback: Check CommunityRole if creator is null (for old communities)
            log.warn("⚠️ Community {} has null creator, falling back to CommunityRole check", communityId);
            isOwner = requester.getCommunityRoles().stream()
                    .anyMatch(role -> role.getCommunity().getId().equals(communityId) 
                            && role.getRole() == CommunityRoles.OWNER);
        }
        
        if (!isOwner) {
            throw ApiRequestException.badRequest("Only community OWNER (creator) can demote moderators");
        }

        // Prevent demoting the OWNER/creator
        if (community.getCreator() != null && community.getCreator().getId().equals(userId)) {
            throw ApiRequestException.badRequest("Cannot demote the community OWNER (creator)");
        }

        // Find the user to demote
        User userToDemote = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Verify user is a member of the community
        if (!community.getUsers().contains(userToDemote)) {
            throw ApiRequestException.badRequest("User is not a member of this community");
        }

        // Check if user is actually a moderator
        boolean isModerator = userToDemote.getCommunityRoles().stream()
                .anyMatch(role -> role.getCommunity().getId().equals(communityId) 
                        && role.getRole() == CommunityRoles.MODERATOR);
        
        if (!isModerator) {
            throw ApiRequestException.badRequest("User is not a moderator");
        }

        // Execute Update: Use native query to update role directly in community_users table
        log.info("🔄 Updating role to MEMBER for user {} in community {} using native query", userId, communityId);
        communityRepository.updateMemberRole(communityId, userId, "MEMBER");
        
        // Also update CommunityRole entity for consistency (remove MODERATOR role)
        List<CommunityRole> moderatorRoles = roleRepository.findByCommunityIdAndRole(communityId, CommunityRoles.MODERATOR);
        if (!moderatorRoles.isEmpty()) {
            CommunityRole moderatorRole = moderatorRoles.get(0);
            if (userToDemote.getCommunityRoles().contains(moderatorRole)) {
                userToDemote.getCommunityRoles().remove(moderatorRole);
                userRepository.save(userToDemote);
            }
        }
        
        log.info("✅ User {} demoted from MODERATOR to MEMBER in community {}. Role updated in database.", userId, communityId);
    }
}
