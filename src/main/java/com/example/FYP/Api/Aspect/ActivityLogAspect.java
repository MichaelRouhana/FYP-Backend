package com.example.FYP.Api.Aspect;


import com.example.FYP.Api.Event.ActivityLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLogAspect {

    private final ApplicationEventPublisher publisher;

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void logActivity(JoinPoint joinPoint) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();

        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";

        String path = request.getRequestURI();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();

        publisher.publishEvent(new ActivityLogEvent(joinPoint, path, method, ip, username));
    }


}

