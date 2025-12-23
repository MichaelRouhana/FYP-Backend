package com.example.AzureTestProject.Api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableCaching
@EnableScheduling

@EnableJpaAuditing
@EnableEnversRepositories
@EnableJpaRepositories(basePackages = {"com.example.AzureTestProject.Api"}, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class AzureTestProjectApplication {


    public static void main(String[] args) {
        SpringApplication.run(AzureTestProjectApplication.class, args);
    }

}
