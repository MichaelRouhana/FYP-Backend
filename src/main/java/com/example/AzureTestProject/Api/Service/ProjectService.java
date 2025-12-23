package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.Organization;
import com.example.AzureTestProject.Api.Entity.Project;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Exception.UserNotFoundException;
import com.example.AzureTestProject.Api.Mapper.ProjectMapper;
import com.example.AzureTestProject.Api.Model.Patch.ProjectPatchDTO;
import com.example.AzureTestProject.Api.Model.Request.ProjectRequestDTO;
import com.example.AzureTestProject.Api.Model.Response.ProjectResponseDTO;
import com.example.AzureTestProject.Api.Repository.OrganizationRepository;
import com.example.AzureTestProject.Api.Repository.ProjectRepository;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import com.example.AzureTestProject.Api.Util.PagedResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ProjectMapper projectMapper;


    public ProjectResponseDTO create(String organizationUUID, ProjectRequestDTO projectRequestDTO) {
        Project project = modelMapper.map(projectRequestDTO, Project.class);

        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));


        project.setOrganization(organization);
        Project savedProject = projectRepository.save(project);

        return modelMapper.map(savedProject, ProjectResponseDTO.class);
    }

    public PagedResponse<Project> getAll(String organizationUUID, Pageable pageable) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));
        return PagedResponse.fromPage(projectRepository.findByOrganizationId(organization.getId(), pageable));
    }

    public Boolean addMember(String organizationUUID, String projectUUID, String email) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        Project project = projectRepository.findByUuidAndOrganizationId(projectUUID, organization.getId())
                .orElseThrow(() -> new EntityNotFoundException("project not found"));

        User invitee = userRepository.findByEmailAndOrganizationId(email, organization.getId())
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        project.getUsers().add(invitee);
        projectRepository.save(project);

        return true;

    }

    public void delete(String organizationUUID, String projectUUID) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        Project project = projectRepository.findByUuidAndOrganizationId(projectUUID, organization.getId())
                .orElseThrow(() -> new EntityNotFoundException("project not found"));

        projectRepository.delete(project);
    }

    public Project get(String organizationUUID, String projectUUID) {
        return projectRepository.findByOrganization_UuidAndUuid(organizationUUID, projectUUID)
                .orElseThrow(() -> new EntityNotFoundException("project not found"));
    }

    public Boolean removeMember(String organizationUUID, String projectUUID, String email) {
        Organization organization = organizationRepository.findByUuid(organizationUUID)
                .orElseThrow(() -> new EntityNotFoundException("organization not found"));

        Project project = projectRepository.findByUuidAndOrganizationId(projectUUID, organization.getId())
                .orElseThrow(() -> new EntityNotFoundException("project not found"));

        User invitee = userRepository.findByEmailAndProjectId(email, project.getId())
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        project.getUsers().remove(invitee);
        projectRepository.save(project);

        return true;
    }

    public void patch(String organizationUUID, String projectUUID, ProjectPatchDTO projectPatchDTO) {
        Project project = projectRepository.findByOrganization_UuidAndUuid(organizationUUID, projectUUID)
                .orElseThrow(() -> new EntityNotFoundException("project not found"));

        projectMapper.updateProjectFromPatchDTO(projectPatchDTO, project, organizationUUID);
        projectRepository.save(project);

    }
}
