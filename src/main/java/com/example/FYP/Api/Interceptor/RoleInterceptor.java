package com.example.FYP.Api.Interceptor;

import com.example.FYP.Api.Entity.Community;
import com.example.FYP.Api.Entity.User;
import com.example.FYP.Api.Interceptor.Annotation.RequiredRole;
import com.example.FYP.Api.Interceptor.Annotation.RequiredRoleTypes;
import com.example.FYP.Api.Interceptor.Annotation.RoleTypeMapping;
import com.example.FYP.Api.Model.Constant.CommunityRoles;
import com.example.FYP.Api.Repository.CommunityRepository;
import com.example.FYP.Api.Security.SecurityContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class RoleInterceptor implements HandlerInterceptor {

    private final SecurityContext securityContext;
    private final CommunityRepository communityRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod method)) return true;

        RequiredRole requiredRole = method.getMethodAnnotation(RequiredRole.class);
        if (requiredRole == null) {
            requiredRole = method.getBeanType().getAnnotation(RequiredRole.class);
        }

        RequiredRoleTypes requiredRoleTypes = method.getMethodAnnotation(RequiredRoleTypes.class);
        if (requiredRole == null && requiredRoleTypes == null) {
            return true;
        }

        String communityIdParam = request.getParameter("communityId");
        if (communityIdParam == null || communityIdParam.isEmpty()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing communityId");
            return false;
        }

        Long communityId;
        try {
            communityId = Long.valueOf(communityIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid communityId");
            return false;
        }


        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found"));

        User user = securityContext.getCurrentUser();

        // Handle single required role
        if (requiredRole != null) {
            Set<CommunityRoles> allowedRoles = Set.of(requiredRole.value());
            boolean hasRole = user.getCommunityRoles().stream()
                    .filter(role -> role.getCommunity().getId() == (community.getId()))
                    .anyMatch(role -> allowedRoles.contains(role.getRole()));

            if (!hasRole) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
                return false;
            }
        }

        // Handle type-based role access
        if (requiredRoleTypes != null) {
            String type = request.getParameter("type");

            if (type == null) {
                response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing 'type' parameter");
                return false;
            }

            boolean authorized = false;
            for (RoleTypeMapping mapping : requiredRoleTypes.value()) {
                if (mapping.type().equalsIgnoreCase(type)) {
                    Set<CommunityRoles> allowedRoles = Set.of(mapping.roles());
                    boolean hasRole = user.getCommunityRoles().stream()
                            .filter(role -> role.getCommunity().getId() == (community.getId()))
                            .anyMatch(role -> allowedRoles.contains(role.getRole()));

                    if (hasRole) {
                        authorized = true;
                        break;
                    }
                }
            }

            if (!authorized) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access denied for type: " + type);
                return false;
            }
        }

        return true;
    }
}
