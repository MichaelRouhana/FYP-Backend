package com.example.FYP.Api.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationService {

/*

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final InvitationRepository invitationRepository;
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
*/

}
