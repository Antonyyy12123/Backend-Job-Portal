package com.ey.service;

import com.ey.dto.LoginRequest;
import com.ey.dto.RegisterRequest;
import com.ey.dto.RegisterResponse;
import com.ey.entity.Role;
import com.ey.entity.User;
import com.ey.exception.ConflictException;
import com.ey.exception.UnauthorizedException;
import com.ey.repository.UserRepository;
import com.ey.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void register_newUser_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John");
        req.setEmail("john@example.com");
        req.setPassword("secret123");
        req.setRole("SEEKER");

        when(userRepo.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(123L);
            return u;
        });

        RegisterResponse resp = authService.register(req);

        assertNotNull(resp.getUserId());
        assertEquals("User registered successfully", resp.getMessage());
    }

    @Test
    void register_existingEmail_throwsConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John");
        req.setEmail("john@example.com");
        req.setPassword("secret123");
        req.setRole("SEEKER");

        when(userRepo.existsByEmail(req.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
    }

    @Test
    void login_validCredentials_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("secret123");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("hashed");
        user.setRole(Role.SEEKER);

        when(userRepo.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getEmail())).thenReturn("jwt-token");

        var resp = authService.login(req);

        assertEquals("Login successful", resp.getMessage());
        assertEquals("jwt-token", resp.getToken());
    }

    @Test
    void login_invalidPassword_throwsUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("wrong");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("hashed");

        when(userRepo.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }
}
