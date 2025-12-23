package com.example.FYP.Api.Mapper;

import com.example.FYP.Api.Entity.CommunityRole;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Model.View.UserViewDTO;
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

    @Mapping(target = "roles", expression = "java(mapOrganizationRoles(user.getCommunityRoles(), organizationId))")
    UserViewDTO toUserViewDTO(User user, @Context long organizationId);

    default List<UserViewDTO> toUserViewDTOList(List<User> users, @Context long organizationId) {
        return users.stream()
                .map(user -> toUserViewDTO(user, organizationId))
                .collect(Collectors.toList());
    }

    default List<String> mapOrganizationRoles(Set<CommunityRole> roles, long organizationId) {
        if (roles == null) return List.of();

        return roles.stream()
                .filter(role -> role.getCommunity() != null && role.getCommunity().getId() == organizationId)
                .map(role -> role.getRole().name())
                .distinct()
                .collect(Collectors.toList());
    }
}
