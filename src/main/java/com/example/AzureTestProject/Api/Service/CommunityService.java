package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.Community;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Model.Filter.CommunityFilterDTO;
import com.example.AzureTestProject.Api.Model.Patch.CommunityPatchDTO;
import com.example.AzureTestProject.Api.Model.Request.CommunityRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.CommunityResponseDTO;
import com.example.AzureTestProject.Api.Model.View.UserViewDTO;
import com.example.AzureTestProject.Api.Repository.CommunityRepository;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import com.example.AzureTestProject.Api.Security.SecurityContext;
import com.example.AzureTestProject.Api.Util.PagedResponse;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final SecurityContext securityContext;
    private final ModelMapper modelMapper;

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

        Community saved = communityRepository.save(community);
        return mapToDTO(saved);
    }

    public CommunityResponseDTO get(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));
        return mapToDTO(community);
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
}
