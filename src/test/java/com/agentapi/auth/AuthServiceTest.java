package com.agentapi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.agentapi.auth.persistence.UsuarioEntity;
import com.agentapi.auth.persistence.UsuarioRepository;
import com.agentapi.web.LoginRequest;
import com.agentapi.web.RegisterRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerReturnsToken() {
        when(usuarioRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(jwtService.createToken(any(), any())).thenReturn("jwt-token");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("a@b.com");
        registerRequest.setPassword("password123");
        var response = authService.register(registerRequest);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
    }

    @Test
    void loginWithValidCredentials() {
        UsuarioEntity user = new UsuarioEntity();
        user.setIdUsuario(java.util.UUID.randomUUID());
        user.setEmail("a@b.com");
        user.setPasswordHash("hash");
        when(usuarioRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtService.createToken(any(), any())).thenReturn("jwt-token");

        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("password123");
        assertThat(authService.login(req).accessToken()).isEqualTo("jwt-token");
    }
}
