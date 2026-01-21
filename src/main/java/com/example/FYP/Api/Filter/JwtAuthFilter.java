package com.example.FYP.Api.Filter;

import com.example.FYP.Api.Service.JwtService;
import com.example.FYP.Api.Service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public JwtAuthFilter(@Lazy UserDetailsServiceImpl userDetailsServiceImpl, JwtService jwtService) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            
            try {
                username = jwtService.extractUsername(token);
            } catch (ExpiredJwtException ex) {
                log.warn("Expired JWT token detected for request to: {} - proceeding as anonymous", request.getRequestURI());
            } catch (MalformedJwtException ex) {
                log.warn("Malformed JWT token detected for request to: {} - proceeding as anonymous", request.getRequestURI());
            } catch (SignatureException ex) {
                log.warn("Invalid JWT signature for request to: {} - proceeding as anonymous", request.getRequestURI());
            } catch (Exception ex) {
                log.warn("JWT parsing error for request to: {} - proceeding as anonymous. Error: {}", 
                        request.getRequestURI(), ex.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception ex) {
                log.warn("Failed to load user details for username: {} - proceeding as anonymous. Error: {}", 
                        username, ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }


}