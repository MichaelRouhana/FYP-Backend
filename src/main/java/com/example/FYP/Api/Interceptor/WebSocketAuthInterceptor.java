package com.example.FYP.Api.Messaging.WebSocket;

import com.example.FYP.Api.Service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.example.FYP.Api.Service.UserDetailsServiceImpl;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // Only handle CONNECT frames
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing Authorization header");
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            // Load UserDetails via UserDetailsService
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println(userDetails.getUsername());

            if (!jwtService.validateToken(token, userDetails)) {
                throw new RuntimeException("Invalid JWT token");
            }

            // Set authenticated Principal for this session
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(auth);
        }

        return message;
    }
}
