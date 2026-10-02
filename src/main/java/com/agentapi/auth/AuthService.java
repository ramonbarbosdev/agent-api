package com.agentapi.auth;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.auth.persistence.UsuarioEntity;
import com.agentapi.auth.persistence.UsuarioRepository;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.web.AuthResponse;
import com.agentapi.web.LoginRequest;
import com.agentapi.web.RegisterRequest;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "E-mail já cadastrado.");
        }
        UsuarioEntity entity = new UsuarioEntity();
        entity.setIdUsuario(UUID.randomUUID());
        entity.setEmail(email);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuarioRepository.save(entity);
        return tokenFor(entity);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        UsuarioEntity entity = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Credenciais inválidas."));
        if (!passwordEncoder.matches(request.getPassword(), entity.getPasswordHash())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Credenciais inválidas.");
        }
        return tokenFor(entity);
    }

    private AuthResponse tokenFor(UsuarioEntity entity) {
        String token = jwtService.createToken(entity.getIdUsuario(), entity.getEmail());
        return new AuthResponse(token, entity.getIdUsuario().toString(), entity.getEmail());
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Informe o e-mail.");
        }
        return email.trim().toLowerCase();
    }
}
