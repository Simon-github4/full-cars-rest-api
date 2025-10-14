package com.fullcars.restapi.service;

import java.rmi.ServerException;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.model.Brand;
import com.fullcars.restapi.model.Category;
import com.fullcars.restapi.repository.ICategoryRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryService {
	
	private ICategoryRepository categoryRepo;
	
	public CategoryService(ICategoryRepository repo) {
		this.categoryRepo = repo;
	}
	@Transactional
	public Category save(Category c) throws ServerException {
		if(c.getId() == null && findByName(c.getName()) != null)
			throw new ServerException("Ya Existe una Categoria con ese nombre");
		else
			return categoryRepo.save(c);
	}
	@Transactional
	public void delete(Long id) {
        categoryRepo.deleteById(id);
	}
	@Transactional
	public Category findByIdOrThrow(Long id) {
		return categoryRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Categoria no encontrada con id: " + id));
	}
	@Transactional(readOnly = true)
	public Category findByName(String name) {
		return categoryRepo.findByName(name);
	}
	@Transactional
	public List<Category> getCategories(){
		return categoryRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	@Transactional
	public Category getCategoryByNameOrCreate(String name) throws ServerException {
		Category b = findByName(name);
		if(b != null)
			return b;
		else
			return save(new Category(null, name));
	}
}
