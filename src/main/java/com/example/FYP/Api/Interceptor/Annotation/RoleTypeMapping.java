package com.example.FYP.Api.Interceptor.Annotation;

import com.example.FYP.Api.Model.Constant.CommunityRoles;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RoleTypeMapping {
    String type();

    CommunityRoles[] roles();
}
