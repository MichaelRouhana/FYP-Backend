package com.example.AzureTestProject.Api.Security;

import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Exception.UserNotFoundException;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityContext {

    private final UserRepository userRepository;

    private SecurityContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new UserNotFoundException("User not found");
        }
        String email = userDetails.getUsername();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
