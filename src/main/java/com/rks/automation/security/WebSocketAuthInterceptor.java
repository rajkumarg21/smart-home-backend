package com.rks.automation.security;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Try Authorization header first
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                log.debug("[WS] Token from STOMP header");
            }

            // Fallback: try query param from session attributes
            if (token == null) {
                Object queryParams = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("token")
                    : null;
                if (queryParams != null) {
                    token = queryParams.toString();
                    log.debug("[WS] Token from session attributes");
                }
            }

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                accessor.setUser(auth);
                log.debug("[WS] Authenticated WebSocket user: {}", username);
            } else {
                log.warn("[WS] Invalid or missing token on CONNECT");
            }
        }

        return message;
    }
}
