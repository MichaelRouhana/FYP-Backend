package com.example.AzureTestProject.Api.Interceptor.Annotation;

import com.example.AzureTestProject.Api.Model.Constant.OrganizationRoles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredRole {
    OrganizationRoles[] value();
}
