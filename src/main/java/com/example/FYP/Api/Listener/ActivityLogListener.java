package com.example.FYP.Api.Listener;


import com.example.FYP.Api.Entity.ActivityLog;
import com.example.FYP.Api.Event.ActivityLogEvent;
import com.example.FYP.Api.Repository.ActivityLogRepository;
import com.nimbusds.openid.connect.sdk.assurance.evidences.Organization;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ActivityLogListener {

    private final ActivityLogRepository activityLogRepository;

    @EventListener
    @Async
    public void handle(ActivityLogEvent event) {
        JoinPoint joinPoint = event.joinPoint();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        String action;
        io.swagger.v3.oas.annotations.Operation operation =
                method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);

        if (operation != null && !operation.summary().isEmpty()) {
            action = operation.summary();
        } else {
            action = method.getName();
        }

        ActivityLog entry = ActivityLog.builder()
                .action(action)
                .path(event.path())
                .httpMethod(event.httpMethod())
                .ip(event.ip())
                .username(event.username())
                .timestamp(LocalDateTime.now())
                .build();

        System.out.println("entry : " + entry.getAction());
        activityLogRepository.save(entry);
    }
}
