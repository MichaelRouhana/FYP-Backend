package com.example.AzureTestProject.Api.Loader.Annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Feature {
    String name() default "";      
    String description() default "";
}
