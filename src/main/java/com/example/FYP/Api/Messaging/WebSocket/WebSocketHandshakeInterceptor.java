package com.example.FYP.Api.Messaging.WebSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("🔌 WebSocket handshake starting for: {}", request.getURI());
        log.info("📋 Request headers: {}", request.getHeaders());
        
        // Extract Authorization header from HTTP request
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null) {
            log.info("🔑 Authorization header found in handshake: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
            attributes.put("authHeader", authHeader);
        } else {
            log.warn("⚠️ No Authorization header in WebSocket handshake request");
        }
        
        return true; // Allow handshake to proceed
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("❌ WebSocket handshake failed: {}", exception.getMessage(), exception);
        } else {
            log.info("✅ WebSocket handshake completed successfully for: {}", request.getURI());
        }
    }
}

