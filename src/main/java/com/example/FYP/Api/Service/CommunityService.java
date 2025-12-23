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
import com.example.FYP.Api.Model.View.CommunityViewDTO;
import com.example.FYP.Api.Model.View.UserViewDTO;
import com.example.FYP.Api.Repository.CommunityRepository;
import com.example.FYP.Api.Repository.CommunityRoleRepository;
import com.example.FYP.Api.Repository.UserRepository;
import com.example.FYP.Api.Security.SecurityContext;
import com.example.FYP.Api.Util.PagedResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public CommunityResponseDTO create(@Valid CommunityRequestDTO communityDTO) {
        Community community = Community.builder()
                .name(communityDTO.getName())
                .logo(communityDTO.getLogo())
                .location(communityDTO.getLocation())
                .about(communityDTO.getAbout())
                .users(new ArrayList<>(List.of(securityContext.getCurrentUser())))
                .rules(communityDTO.getRules())
                .build();

        CommunityRole role = CommunityRole.builder().role(CommunityRoles.OWNER).community(community).build();
        CommunityRole roleMember = CommunityRole.builder().role(CommunityRoles.MEMBER).community(community).build();

        User user = securityContext.getCurrentUser();

        user.getCommunityRoles().addAll(new HashSet<>(List.of(role, roleMember)));

        community.setRoles(new HashSet<>(List.of(role, roleMember)));

        Community saved = communityRepository.save(community);
        return mapToDTO(saved);
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

    public CommunityViewDTO get(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
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

    private CommunityResponseDTO mapToDTO(Community community) {
        return CommunityResponseDTO.builder()
                .id(community.getId())
                .name(community.getName())
                .logo(community.getLogo())
                .location(community.getLocation())
                .about(community.getAbout())
                .rules(community.getRules())
                .userIds(community.getUsers().stream().map(User::getId).toList())
                .build();
    }

    private CommunityViewDTO mapToDTOView(Community community) {
        return CommunityViewDTO.builder()
                .id(community.getId())
                .name(community.getName())
                .logo(community.getLogo())
                .location(community.getLocation())
                .about(community.getAbout())
                .rules(community.getRules())
                .userIds(community.getUsers().stream().map(User::getId).toList())
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
}
