package com.example.FYP.Api;

import com.example.FYP.Api.Entity.AppConfig;
import com.example.FYP.Api.Entity.UserRole;
import com.example.FYP.Api.Model.Constant.Role;
import com.example.FYP.Api.Repository.AppConfigRepository;
import com.example.FYP.Api.Repository.RoleRepository;
import com.example.FYP.Api.Repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserService userService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        if (appConfigRepository.existsById("INITIALIZED")) {
            log.info("Initialization already done. Skipping...");
            return;
        }

        log.info("Starting initialization...");

        // Create all roles (USER, ADMIN, DEVELOPER)
        UserRole roleUser = UserRole.builder().role(Role.USER).build();
        UserRole roleAdmin = UserRole.builder().role(Role.ADMIN).build();
        UserRole roleDeveloper = UserRole.builder().role(Role.DEVELOPER).build();

        roleRepository.saveAll(List.of(roleUser, roleAdmin, roleDeveloper));
        log.info("Roles saved.");

        // Create admin users (only if they don't already exist)
        createUserIfNotExists("admin", "admin@fyp.com", "password123", Role.ADMIN);
        createUserIfNotExists("ahla", "ahlacom80@gmail.com", "SecurePassword1234", Role.ADMIN);
        createUserIfNotExists("Michael", "michaelrouhana@gmail.com", "SecurePassword1234", Role.ADMIN);


        appConfigRepository.save(new AppConfig("INITIALIZED", "true"));

        log.info("Initialization completed successfully.");
    }

    private void createUserIfNotExists(String username, String email, String password, Role role) {
        if (userRepository.findByEmail(email.toLowerCase()).isEmpty()) {
            userService.createUser(username, email, password, role);
            log.info("Created user: {}", email);
        } else {
            log.info("User already exists, skipping: {}", email);
        }
    }
}
