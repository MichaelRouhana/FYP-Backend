package com.example.FYP.Api.Messaging.WebSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final com.example.FYP.Api.Messaging.WebSocket.WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Raw WebSocket endpoint for React Native and other native clients
        // Note: With servlet context-path=/api/v1, this endpoint becomes /api/v1/ws
        // Register at /ws, but clients must connect to /api/v1/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // ✅ Use this ONLY
                .addInterceptors(new WebSocketHandshakeInterceptor());
        
        // SockJS endpoint for web browsers (fallback)
        registry.addEndpoint("/ws/sockjs")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new WebSocketHandshakeInterceptor())
                .withSockJS();
        
        System.out.println("✅ WebSocket endpoints registered:");
        System.out.println("   - /ws (becomes /api/v1/ws with context-path)");
        System.out.println("   - /ws/sockjs (becomes /api/v1/ws/sockjs with context-path)");
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Configure thread pool for better debugging
        registration.taskExecutor().corePoolSize(1).maxPoolSize(1);
        // Add interceptor with highest priority
        registration.interceptors(webSocketAuthInterceptor); // add JWT interceptor
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Also log outbound messages for debugging
        registration.taskExecutor().corePoolSize(1).maxPoolSize(1);
    }

}
