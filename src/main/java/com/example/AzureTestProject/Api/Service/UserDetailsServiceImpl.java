package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.CustomUserDetails;
import com.example.AzureTestProject.Api.Entity.User;
import com.example.AzureTestProject.Api.Exception.UserNotFoundException;
import com.example.AzureTestProject.Api.Repository.RoleRepository;
import com.example.AzureTestProject.Api.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserDetailsServiceImpl implements UserDetailsService {


    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        System.out.println(user.isVerified());
        return new CustomUserDetails(user);
    }
}