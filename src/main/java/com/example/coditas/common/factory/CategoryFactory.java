package com.example.coditas.common.factory;

import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CategoryFactory {
    private final Map<String, CategoryService> categoryService;

    public CategoryFactory(Map<String, CategoryService> categoryService) {
        this.categoryService = categoryService;
    }

    public CategoryService getCategoryService(String type){
        CategoryService service = categoryService.get(type.toUpperCase());
        if(service==null){
            throw new CustomException("Unknown category type: " + type, HttpStatus.BAD_REQUEST);
        }
        return service;
    }
}
