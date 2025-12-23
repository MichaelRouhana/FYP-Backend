package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.*;
import com.example.AzureTestProject.Api.Exception.*;
import com.example.AzureTestProject.Api.Mapper.OrganizationMapper;
import com.example.AzureTestProject.Api.Mapper.UserMapper;
import com.example.AzureTestProject.Api.Messaging.Model.InvitationMessage;
import com.example.AzureTestProject.Api.Messaging.RabbitMqProducer;
import com.example.AzureTestProject.Api.Model.Constant.*;
import com.example.AzureTestProject.Api.Model.Patch.OrganizationPatchDTO;
import com.example.AzureTestProject.Api.Model.Request.*;
import com.example.AzureTestProject.Api.Model.Response.InvitationResponseDTO;
import com.example.AzureTestProject.Api.Model.Response.OrganizationResponseDTO;
import com.example.AzureTestProject.Api.Model.View.UserViewDTO;
import com.example.AzureTestProject.Api.Repository.InvitationRepository;
import com.example.AzureTestProject.Api.Repository.OrganizationRepository;
import com.example.AzureTestProject.Api.Repository.OrganizationRoleRepository;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import com.example.AzureTestProject.Api.Security.SecurityContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationService {


    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final InvitationRepository invitationRepository;
    private final OrganizationRoleRepository roleRepository;
    private final RabbitMqProducer rabbitMqProducer;
    private final UserService userService;
    private final SecurityContext securityContext;
    private final OrganizationMapper organizationMapper;
    private final UserMapper userMapper;
    private final SubscriptionService subscriptionService;

    @Transactional
    public OrganizationResponseDTO create(OrganizationRequestDTO organizationRequestDTO) {
        User user = securityContext.getCurrentUser();

        if (organizationRepository.existsByOwner(user)) {
            throw new OrganizationAlreadyExistException("user already has an organization");
        }

        Organization organization = modelMapper.map(organizationRequestDTO, Organization.class);
        organization.setOwner(user);
        organization.setUsers(new ArrayList<>(Collections.singleton(user)));
        organization.setVat(organizationRequestDTO.getVat());
        OrganizationRole role = OrganizationRole.builder().role(OrganizationRoles.OWNER).organization(organization).build();
        OrganizationRole roleMember = OrganizationRole.builder().role(OrganizationRoles.MEMBER).organization(organization).build();
        OrganizationRole roleHr = OrganizationRole.builder().role(OrganizationRoles.HR).organization(organization).build();
        OrganizationRole roleAp = OrganizationRole.builder().role(OrganizationRoles.AP).organization(organization).build();
        OrganizationRole roleAr = OrganizationRole.builder().role(OrganizationRoles.AR).organization(organization).build();
        OrganizationRole roleFinance = OrganizationRole.builder().role(OrganizationRoles.FINANCE).organization(organization).build();
        OrganizationRole rolePurchasing = OrganizationRole.builder().role(OrganizationRoles.PURCHASING).organization(organization).build();
        OrganizationRole roleProcurement = OrganizationRole.builder().role(OrganizationRoles.PROCUREMENT).organization(organization).build();
        OrganizationRole roleProduction = OrganizationRole.builder().role(OrganizationRoles.PRODUCTION).organization(organization).build();
        OrganizationRole roleAccountingFinance = OrganizationRole.builder().role(OrganizationRoles.ACCOUNTING_FINANCE).organization(organization).build();

        organization.setRoles(new HashSet<>(List.of(role, roleMember, roleHr, roleAp, roleAr, roleFinance, rolePurchasing, roleProcurement, roleProduction, roleAccountingFinance)));

        Organization savedOrganization = organizationRepository.save(organization);

        subscriptionService.subscribe(organization, Plan.SubscriptionPlan.ENTERPRISE);

        user.getOrganizationRoles().addAll(new HashSet<>(List.of(role, roleMember)));



        return modelMapper.map(savedOrganization, OrganizationResponseDTO.class);
    }

    //@Transactional(rollbackOn = Exception.class)
    public InvitationResponseDTO invite(String organizationUUID, InvitationRequestDTO invitationRequestDTO) {

        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        if (invitationRepository.existsByInviteeEmail(invitationRequestDTO.getInviteeEmail()))
            throw new InvitationAlreadyExistException("This user is already invited");
        Optional<User> u = userRepository.findByEmailAndOrganizationId(invitationRequestDTO.getInviteeEmail(), organization.getId());

        if (u.isPresent()) throw new UserAlreadyInvitedException("user already invited");

        Invitation invitation = modelMapper.map(invitationRequestDTO, Invitation.class);

        User invitee = userRepository.findByEmail(invitationRequestDTO.getInviteeEmail())
                .orElseThrow(() -> new UserNotFoundException("user not found exception"));

        invitation.setInvitee(invitee);
        invitation.setOrganizationUUID(organizationUUID);
        invitation.setUuid(UUID.randomUUID().toString());


        Invitation savedInvitation = invitationRepository.save(invitation);

        InvitationMessage message = InvitationMessage.builder()
                .token(organization.getUuid())
                .email(invitee.getEmail())
                .build();

        rabbitMqProducer.sendInvite("inviteQueue", message);

        return modelMapper.map(savedInvitation, InvitationResponseDTO.class);

    }

    @Transactional
    public void acceptInvitation(String invitationUUID) {
        User user = securityContext.getCurrentUser();

        Invitation invitation = invitationRepository.findByUuid(invitationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (!invitation.getInvitee().getEmail().equals(user.getEmail()))
            throw ApiRequestException.badRequest("not invited");

        Organization organization = organizationRepository.findByUuid(invitation.getOrganizationUUID())
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        organization.getUsers().add(invitation.getInvitee());

        user.getOrganizationRoles().addAll(roleRepository.findByRoleIn(List.of(OrganizationRoles.MEMBER)));


        organizationRepository.save(organization);

        invitationRepository.delete(invitation);

    }

    public void delete(String organizationUUID) {
        userService.hasRole(organizationUUID, List.of("OWNER"));

        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        organizationRepository.delete(organization);


    }

    public List<InvitationResponseDTO> getInvitations() {
        User user = securityContext.getCurrentUser();
        return invitationRepository.findByInvitee(user)
                .stream()
                .map(invitation -> modelMapper.map(invitation, InvitationResponseDTO.class))
                .collect(Collectors.toList());
    }

    public OrganizationResponseDTO get(String organizationUUID) {


        return modelMapper.map(organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found")), OrganizationResponseDTO.class);
    }

    public void revokeRole(String organizationUUID, String email, List<OrganizationRoles> roles) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        User userInfo = userRepository.findByEmailAndOrganizationId(email, organization.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<OrganizationRole> rolesToRevoke = roleRepository.findByRoleIn(roles);

        if (rolesToRevoke.isEmpty()) {
            throw new EntityNotFoundException("Roles not found");
        }

        rolesToRevoke.forEach(role -> {
            userInfo.getOrganizationRoles().remove(role);
        });

        userRepository.save(userInfo);
    }

    public void assignRole(String organizationUUID, String email, List<OrganizationRoles> roles) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        User userInfo = userRepository.findByEmailAndOrganizationId(email, organization.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<OrganizationRole> rolesToAssign = roleRepository.findByRoleIn(roles);

        if (rolesToAssign.isEmpty()) {
            throw new EntityNotFoundException("Roles not found");
        }

        rolesToAssign.forEach(role -> {
            if (!userInfo.getRoles().contains(role)) {
                userInfo.getOrganizationRoles().add(role);
            }
        });

        userRepository.save(userInfo);
    }

    public List<String> getRoles(String organizationUUID, String email) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        userRepository.findByEmailAndOrganizationId(email, organization.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return roleRepository.findRolesByUserAndOrganization(email, organizationUUID);
    }

    public void patch(String organizationUUID, OrganizationPatchDTO organizationPatchDTO) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        organizationMapper.updateOrganizationFromDto(organizationPatchDTO, organization);
        organizationRepository.save(organization);
    }

    public UserViewDTO me(String organizationUUID) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        User user = securityContext.getCurrentUser();
        return userMapper.toUserViewDTO(user, organization.getId());
    }

    @Transactional
    public void removeUser(String organizationUUID, String email) {
        userService.hasRole(organizationUUID, List.of("OWNER"));

        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        User user = organization.getUsers().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));

        user.getOrganizationRoles().clear();

        userRepository.save(user);

        organization.getUsers().remove(user);

        organizationRepository.save(organization);

    }

    public void assignTier(String organizationUUID, String user,  List<String> tiers) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        User u = userRepository.findByEmailAndOrganizationId(user, organization.getId())
                .orElseThrow(() -> new EntityNotFoundException("user not found: " + user));

        List<String> errors = new ArrayList<>();

        List<Tier> tiersList = tiers.stream()
                .map(tierStr -> {
                    try {
                        return Tier.valueOf(tierStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        errors.add("Invalid tier: " + tierStr);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if(!errors.isEmpty()) throw ApiRequestException.badRequest("Invalid tiers: " + errors + "Allowed Tiers: " + Arrays.toString(Tier.values()));


    }

}
