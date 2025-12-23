package com.example.AzureTestProject.Api.Mapper;


import com.example.AzureTestProject.Api.Entity.Project;
import com.example.AzureTestProject.Api.Model.Patch.ProjectPatchDTO;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ProjectMapper {



    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void updateProjectFromPatchDTO(ProjectPatchDTO dto, @MappingTarget Project project, @Context String organizationUUID) {
    }

}
