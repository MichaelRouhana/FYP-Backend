package com.example.AzureTestProject.Api.Interceptor.Annotation;

import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RoleTypeMapping {
    String type();

    OrganizationRoles[] roles();
}
