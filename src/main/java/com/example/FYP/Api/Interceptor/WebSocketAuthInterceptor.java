package com.example.FYP.Api.Interceptor;

import com.example.FYP.Api.Service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            // Log ALL messages for debugging
            log.info("📥 preSend called - Message type: {}, Channel: {}", 
                    message.getClass().getSimpleName(), 
                    channel != null ? channel.getClass().getSimpleName() : "null");
            
            StompHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor == null) {
                log.warn("⚠️ StompHeaderAccessor is null, skipping interceptor. Message headers: {}", message.getHeaders());
                return message;
            }

            StompCommand command = accessor.getCommand();
            log.info("📨 Interceptor called for STOMP command: {}", command);
            
            // Log all commands for debugging
            if (command != null) {
                log.info("📋 STOMP Command: {}, Session: {}, User: {}, All headers: {}", 
                        command, 
                        accessor.getSessionId(), 
                        accessor.getUser() != null ? accessor.getUser().getName() : "null",
                        accessor.toNativeHeaderMap());
            } else {
                log.warn("⚠️ STOMP command is null! Message headers: {}", accessor.toNativeHeaderMap());
            }

        // Only handle CONNECT frames
        if (StompCommand.CONNECT.equals(command)) {
            log.info("🔌 WebSocket CONNECT attempt received");
            log.info("📋 All headers: {}", accessor.toNativeHeaderMap());
            
            try {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                log.info("🔑 Authorization header present: {}", authHeader != null);
                if (authHeader != null) {
                    log.info("🔑 Authorization header value: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
                }

                // Safer check handling nulls and short strings
                if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() < 7) {
                    log.error("❌ Missing or invalid Authorization header in WebSocket CONNECT. Header: {}", authHeader);
                    throw new RuntimeException("Missing or invalid Authorization header");
                }

                String token = authHeader.substring(7);
                log.info("🔓 Extracting username from token...");
                
                String username = jwtService.extractUsername(token);
                log.info("👤 Username extracted: {}", username);

                // Load UserDetails via UserDetailsService
                log.info("📚 Loading user details for: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.info("✅ UserDetails loaded for: {}", userDetails.getUsername());

                log.info("🔍 Validating JWT token...");
                if (!jwtService.validateToken(token, userDetails)) {
                    log.error("❌ JWT token validation failed for user: {}", username);
                    throw new RuntimeException("Invalid JWT token");
                }

                log.info("✅ JWT token validated successfully for user: {}", username);

                // Set authenticated Principal for this session
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                accessor.setUser(auth);
                log.info("🎉 WebSocket authentication successful for user: {}", username);
            } catch (Exception e) {
                log.error("❌ WebSocket authentication failed: {}", e.getMessage(), e);
                log.error("❌ Exception class: {}", e.getClass().getName());
                log.error("❌ Stack trace:", e);
                // Re-throw to let Spring handle the error response
                throw new RuntimeException("WebSocket authentication failed: " + e.getMessage(), e);
            }
        }

            return message;
        } catch (Exception e) {
            log.error("❌ Exception in preSend interceptor: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            log.error("❌ Error after sending STOMP message. Command: {}, Session: {}, Error: {}", 
                    accessor != null ? accessor.getCommand() : "unknown",
                    accessor != null ? accessor.getSessionId() : "unknown",
                    ex.getMessage(), ex);
        }
    }
}
