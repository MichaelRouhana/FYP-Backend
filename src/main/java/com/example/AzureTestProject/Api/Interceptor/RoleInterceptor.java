package com.example.AzureTestProject.Api.Interceptor;

import com.example.AzureTestProject.Api.Entity.Organization;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Interceptor.Annotation.RequiredRole;
import com.example.AzureTestProject.Api.Interceptor.Annotation.RequiredRoleTypes;
import com.example.AzureTestProject.Api.Interceptor.Annotation.RoleTypeMapping;
import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;
import com.example.AzureTestProject.Api.Repository.OrganizationRepository;
import com.example.AzureTestProject.Api.Security.SecurityContext;
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
    private final OrganizationRepository organizationRepository;

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

        String orgUUID = request.getParameter("organizationUUID");
        if (orgUUID == null) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing organizationUUID");
            return false;
        }

        Organization organization = organizationRepository.findByUuid(orgUUID)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        User user = securityContext.getCurrentUser();

        // Handle single required role
        if (requiredRole != null) {
            Set<OrganizationRoles> allowedRoles = Set.of(requiredRole.value());
            boolean hasRole = user.getOrganizationRoles().stream()
                    .filter(role -> role.getOrganization().getId() == (organization.getId()))
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
                    Set<OrganizationRoles> allowedRoles = Set.of(mapping.roles());
                    boolean hasRole = user.getOrganizationRoles().stream()
                            .filter(role -> role.getOrganization().getId() == (organization.getId()))
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
