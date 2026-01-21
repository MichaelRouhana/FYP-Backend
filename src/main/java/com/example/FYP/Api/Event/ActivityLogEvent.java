package com.example.FYP.Api.Event;

import org.aspectj.lang.JoinPoint;

public record ActivityLogEvent(
        JoinPoint joinPoint,
        String path,
        String httpMethod,
        String ip,
        String username
) {}
