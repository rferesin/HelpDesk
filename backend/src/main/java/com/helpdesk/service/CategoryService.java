package com.helpdesk.service;

import com.helpdesk.domain.exception.BusinessException;
import com.helpdesk.domain.exception.ResourceNotFoundException;
import com.helpdesk.domain.model.Category;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.web.dto.TicketDtos.CategoryRequest;
import com.helpdesk.web.dto.TicketDtos.CategoryResponse;
import com.helpdesk.web.mapper.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DtoMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, DtoMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(mapper::toCategory).toList();
    }

    public List<CategoryResponse> findActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream().map(mapper::toCategory).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Categoria ja existe");
        }
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setActive(true);
        return mapper.toCategory(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = require(id);
        categoryRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Categoria ja existe");
                });
        category.setName(request.name());
        category.setDescription(request.description());
        return mapper.toCategory(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse toggle(Long id) {
        Category category = require(id);
        category.setActive(!category.isActive());
        return mapper.toCategory(categoryRepository.save(category));
    }

    public Category require(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
    }
}
