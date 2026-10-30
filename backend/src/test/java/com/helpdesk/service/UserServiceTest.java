package com.helpdesk.service;

import com.helpdesk.domain.enums.Role;
import com.helpdesk.domain.exception.BusinessException;
import com.helpdesk.domain.model.User;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.web.dto.AuthDtos.CreateUserRequest;
import com.helpdesk.web.dto.AuthDtos.UpdateUserRequest;
import com.helpdesk.web.mapper.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, new DtoMapper());
    }

    @Test
    void createRejectsDuplicatedEmail() {
        when(userRepository.existsByEmailIgnoreCase("ana@helpdesk.local")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.create(
                new CreateUserRequest("Ana", "ana@helpdesk.local", "senha123", Role.ATENDENTE)));
    }

    @Test
    void createPersistsEncodedPasswordAndDefaultRole() {
        when(userRepository.existsByEmailIgnoreCase("novo@helpdesk.local")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(15L);
            return user;
        });

        var response = userService.create(new CreateUserRequest("Novo", "novo@helpdesk.local", "senha123", null));

        assertEquals(Role.CLIENTE, response.role());
        assertEquals("novo@helpdesk.local", response.email());
    }

    @Test
    void toggleActiveInvertsFlag() {
        User user = new User();
        user.setId(1L);
        user.setName("Carlos");
        user.setEmail("carlos@helpdesk.local");
        user.setRole(Role.CLIENTE);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.toggleActive(1L);

        assertFalse(response.active());
    }

    @Test
    void updateChangesNameAndRole() {
        User user = new User();
        user.setId(2L);
        user.setName("Ana");
        user.setEmail("ana@helpdesk.local");
        user.setRole(Role.CLIENTE);
        user.setActive(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.update(2L, new UpdateUserRequest("Ana Atendente", Role.ATENDENTE, true));

        assertEquals("Ana Atendente", response.name());
        assertEquals(Role.ATENDENTE, response.role());
    }
}
