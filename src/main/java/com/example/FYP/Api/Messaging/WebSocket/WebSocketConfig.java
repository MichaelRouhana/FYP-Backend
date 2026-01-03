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
        // Note: WebSocket endpoints are NOT affected by servlet context-path
        // They are registered at the root level, so use /ws (not /api/v1/ws)
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // Allow all origins for React Native
                .setAllowedOriginPatterns("*");
        
        // SockJS endpoint for web browsers (fallback)
        registry.addEndpoint("/ws/sockjs")
                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor); // add JWT interceptor
    }

}
