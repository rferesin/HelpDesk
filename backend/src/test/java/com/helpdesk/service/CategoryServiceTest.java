package com.helpdesk.service;

import com.helpdesk.domain.exception.BusinessException;
import com.helpdesk.domain.model.Category;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.web.dto.TicketDtos.CategoryRequest;
import com.helpdesk.web.mapper.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, new DtoMapper());
    }

    @Test
    void createRejectsDuplicatedName() {
        when(categoryRepository.existsByNameIgnoreCase("Rede")).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                categoryService.create(new CategoryRequest("Rede", "VPN")));
    }

    @Test
    void toggleInvertsActiveFlag() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Rede");
        category.setActive(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = categoryService.toggle(1L);

        assertFalse(response.active());
        assertEquals("Rede", response.name());
    }
}
