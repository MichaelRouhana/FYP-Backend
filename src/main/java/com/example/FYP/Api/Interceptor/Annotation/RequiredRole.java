package com.example.FYP.Api.Interceptor.Annotation;

import com.example.FYP.Api.Model.Constant.CommunityRoles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredRole {
    CommunityRoles[] value();
}
