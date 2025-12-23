package com.example.FYP.Api;

import com.example.FYP.Api.Entity.AppConfig;
import com.example.FYP.Api.Entity.UserRole;
import com.example.FYP.Api.Model.Constant.Role;
import com.example.FYP.Api.Repository.AppConfigRepository;
import com.example.FYP.Api.Repository.RoleRepository;
import com.example.FYP.Api.Service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final AppConfigRepository appConfigRepository;
    private final UserService userService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        if (appConfigRepository.existsById("INITIALIZED")) {
            log.info("Initialization already done. Skipping...");
            return;
        }

        log.info("Starting initialization...");


        UserRole roleUser = UserRole.builder().role(Role.USER).build();
        UserRole roleDeveloper = UserRole.builder().role(Role.DEVELOPER).build();


        log.info("Roles saved.");
        roleRepository.saveAll(List.of(roleUser, roleDeveloper));

        userService.createUser("ahla", "Ahlacom80@gmail.com", "SecurePassword1234", Role.ADMIN);
        userService.createUser("Michael", "MichaelRouhana@gmail.com", "SecurePassword1234", Role.ADMIN);


        appConfigRepository.save(new AppConfig("INITIALIZED", "true"));

        log.info("Initialization completed successfully.");
    }
}
