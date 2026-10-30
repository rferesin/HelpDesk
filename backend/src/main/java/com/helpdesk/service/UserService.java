package com.helpdesk.service;

import com.helpdesk.domain.enums.Role;
import com.helpdesk.domain.exception.BusinessException;
import com.helpdesk.domain.exception.ResourceNotFoundException;
import com.helpdesk.domain.model.User;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.web.dto.AuthDtos.CreateUserRequest;
import com.helpdesk.web.dto.AuthDtos.UpdateUserRequest;
import com.helpdesk.web.dto.AuthDtos.UserResponse;
import com.helpdesk.web.mapper.DtoMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper mapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, DtoMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(mapper::toUser).toList();
    }

    public List<UserResponse> findByRole(Role role) {
        return userRepository.findByRole(role).stream().map(mapper::toUser).toList();
    }

    public UserResponse findById(Long id) {
        return mapper.toUser(requireUser(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Email ja cadastrado");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : Role.CLIENTE);
        user.setActive(true);
        return mapper.toUser(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = requireUser(id);
        user.setName(request.name());
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        return mapper.toUser(userRepository.save(user));
    }

    @Transactional
    public UserResponse toggleActive(Long id) {
        User user = requireUser(id);
        user.setActive(!user.isActive());
        return mapper.toUser(userRepository.save(user));
    }

    public User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    public User requireByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
