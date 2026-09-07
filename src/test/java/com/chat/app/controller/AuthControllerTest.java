package com.chat.app.controller;

import com.chat.app.dto.AuthRequest;
import com.chat.app.dto.RegisterRequest;
import com.chat.app.model.User;
import com.chat.app.repository.UserRepository;
import com.chat.app.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(jwtUtils.generateToken("testuser")).thenReturn("mock-jwt-token");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        ResponseEntity<?> response = authController.registerUser(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateUsername() {
        RegisterRequest req = RegisterRequest.builder()
                .username("existinguser")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        AuthRequest req = AuthRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);
        when(jwtUtils.generateToken("testuser")).thenReturn("mock-jwt-token");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        ResponseEntity<?> response = authController.authenticateUser(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLoginBadCredentials() {
        AuthRequest req = AuthRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.authenticateUser(req);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
