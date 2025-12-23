package com.example.FYP.Api.Mapper;

import com.example.FYP.Api.Entity.Organization;
import com.example.FYP.Api.Model.Patch.OrganizationPatchDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrganizationFromDto(OrganizationPatchDTO dto, @MappingTarget Organization organization);

}
