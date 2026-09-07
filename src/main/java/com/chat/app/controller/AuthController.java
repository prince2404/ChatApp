package com.chat.app.controller;

import com.chat.app.dto.ApiResponse;
import com.chat.app.dto.AuthRequest;
import com.chat.app.dto.AuthResponse;
import com.chat.app.dto.RegisterRequest;
import com.chat.app.model.User;
import com.chat.app.repository.UserRepository;
import com.chat.app.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        String username = registerRequest.getUsername().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Username '" + username + "' is already taken!"));
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("Registered new user: {}", username);

        // Auto-login upon successful registration by issuing a token
        String jwt = jwtUtils.generateToken(username);
        AuthResponse response = AuthResponse.builder()
                .token(jwt)
                .username(username)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationMs())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        String username = loginRequest.getUsername().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(username);

            AuthResponse response = AuthResponse.builder()
                    .token(jwt)
                    .username(username)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtils.getExpirationMs())
                    .build();

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            log.error("Authentication error for user {}: {}", username, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during authentication"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }

        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }
}
