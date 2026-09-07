package com.chat.app.security.websocket;

import com.chat.app.security.jwt.JwtUtils;
import com.chat.app.security.service.UserDetailsServiceImpl;
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
import org.springframework.util.StringUtils;

/**
 * ChannelInterceptor for STOMP messages.
 * Checks for a JWT token in STOMP headers on CONNECT frames:
 * - If token is present and valid, attaches authenticated UserDetails Principal to the session.
 * - If no token is provided, permits the connection as a guest/lobby room chatter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                accessor.setUser(authentication);
                log.info("WebSocket STOMP connected for authenticated user: {}", username);
            } else {
                // Allows lobby users entering rooms by name to connect smoothly
                log.info("WebSocket STOMP connected for lobby room user");
            }
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // Check "Authorization: Bearer <token>"
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Check "token" header
        String tokenHeader = accessor.getFirstNativeHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }

        // Fallback: check session attributes (from handshake query parameters)
        if (accessor.getSessionAttributes() != null) {
            Object tokenAttr = accessor.getSessionAttributes().get("token");
            if (tokenAttr != null) {
                return tokenAttr.toString();
            }
        }

        return null;
    }
}
