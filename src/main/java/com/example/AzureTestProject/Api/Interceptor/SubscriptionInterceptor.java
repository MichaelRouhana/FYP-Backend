package com.example.AzureTestProject.Api.Interceptor;

import com.example.AzureTestProject.Api.Entity.Organization;
import com.example.AzureTestProject.Api.Entity.Plan;
import com.example.AzureTestProject.Api.Entity.Subscription;
import com.example.AzureTestProject.Api.Exception.ApiException;
import com.example.AzureTestProject.Api.Repository.OrganizationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.ZonedDateTime;

@Component
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final OrganizationRepository organizationRepository;

    public SubscriptionInterceptor(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }



        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {

            String uri = request.getRequestURI();

            if (uri.startsWith("/error") || uri.startsWith("/actuator") || uri.startsWith("/swagger")) {
                return true;
            }

            String orgUuid = request.getParameter("organizationUUID");
            if (orgUuid == null || orgUuid.isEmpty()) {
                writeApiError(response, "organizationUUID is required", HttpStatus.BAD_REQUEST, request);
                return false;
            }

            Organization org = organizationRepository.findByUuid(orgUuid)
                    .orElseThrow(() -> new EntityNotFoundException("organization not found"));

            Subscription subscription = org.getSubscription();
            if (subscription == null) {
                writeApiError(response, "Organization has no active subscription", HttpStatus.FORBIDDEN, request);
                return false;
            }

            if (subscription.getStatus() != Subscription.SubscriptionStatus.ACTIVE) {
                writeApiError(response, "Subscription is: " + subscription.getStatus(), HttpStatus.PAYMENT_REQUIRED, request);
                return false;
            }

            String controllerPath = "/";
            if (handler instanceof HandlerMethod handlerMethod) {
                RequestMapping classMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
                RequestMapping methodMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);

                String classPath = (classMapping != null && classMapping.value().length > 0) ? classMapping.value()[0] : "";
                String methodPath = (methodMapping != null && methodMapping.value().length > 0) ? methodMapping.value()[0] : "";

                controllerPath = (classPath + methodPath).replaceAll("//", "/");
                if (!controllerPath.startsWith("/")) controllerPath = "/" + controllerPath;
            }

            Plan plan = subscription.getPlan();
            String finalControllerPath = controllerPath;

            System.out.println(finalControllerPath);
            boolean allowed = plan.getFeatures().stream()
                    .anyMatch(f -> finalControllerPath.startsWith(f.getPattern()));

            if (!allowed) {
                writeApiError(response, "Plan does not allow access to this endpoint", HttpStatus.FORBIDDEN, request);
                return false;
            }

            return true;
        }

        private void writeApiError(HttpServletResponse response, String message, HttpStatus status, HttpServletRequest request) throws IOException {
            ApiException apiException = ApiException.builder()
                    .zonedDateTime(ZonedDateTime.now())
                    .message(message)
                    .status(status)
                    .code(status.value())
                    .path(request.getRequestURI())
                    .build();

            response.setStatus(status.value());
            response.setContentType("application/json");

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            response.getWriter().write(mapper.writeValueAsString(apiException));
            response.getWriter().flush();
    }


}
