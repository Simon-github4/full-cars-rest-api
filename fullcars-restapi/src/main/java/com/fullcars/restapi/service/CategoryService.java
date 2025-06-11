package com.fullcars.restapi.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.fullcars.restapi.model.Category;
import com.fullcars.restapi.repository.ICategoryRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryService {
	
	private ICategoryRepository categoryRepo;
	
	public CategoryService(ICategoryRepository repo) {
		this.categoryRepo = repo;
	}
	
	public Category save(Category c) {
		return categoryRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!categoryRepo.existsById(id)) 
            throw new EntityNotFoundException("Categoria no encontrada con id: " + id);
        categoryRepo.deleteById(id);
	}
	
	public Category findByIdOrThrow(Long id) {
		return categoryRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Categoria no encontrada con id: " + id));
	}
	
	public List<Category> getCategories(){
		return categoryRepo.findAll();
	}
}
