package com.helpdesk.service;

import com.helpdesk.domain.exception.BusinessException;
import com.helpdesk.domain.model.User;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.JwtService;
import com.helpdesk.web.dto.AuthDtos.LoginRequest;
import com.helpdesk.web.dto.AuthDtos.LoginResponse;
import com.helpdesk.web.dto.AuthDtos.UserResponse;
import com.helpdesk.web.mapper.DtoMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final DtoMapper mapper;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService,
                       DtoMapper mapper) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException("Credenciais invalidas"));
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", mapper.toUser(user));
    }

    public UserResponse me(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
        return mapper.toUser(user);
    }
}
