package com.example.AzureTestProject.Api.Mapper;

import com.example.AzureTestProject.Api.Entity.OrganizationRole;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Model.View.UserViewDTO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "roles", expression = "java(mapOrganizationRoles(user.getOrganizationRoles(), organizationId))")
    UserViewDTO toUserViewDTO(User user, @Context long organizationId);

    default List<UserViewDTO> toUserViewDTOList(List<User> users, @Context long organizationId) {
        return users.stream()
                .map(user -> toUserViewDTO(user, organizationId))
                .collect(Collectors.toList());
    }

    default List<String> mapOrganizationRoles(Set<OrganizationRole> roles, long organizationId) {
        if (roles == null) return List.of();

        return roles.stream()
                .filter(role -> role.getOrganization() != null && role.getOrganization().getId() == organizationId)
                .map(role -> role.getRole().name())
                .distinct()
                .collect(Collectors.toList());
    }
}
