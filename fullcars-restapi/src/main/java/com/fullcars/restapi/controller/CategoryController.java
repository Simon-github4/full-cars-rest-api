package com.fullcars.restapi.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fullcars.restapi.model.Category;
import com.fullcars.restapi.service.CategoryService;

@RestController
@RequestMapping(value = "/categories")
public class CategoryController {

private CategoryService categoryService;
	
	public CategoryController(CategoryService repo) {
		this.categoryService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Category post(@RequestBody Category b) {
		return categoryService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Category put(@PathVariable Long id, @RequestBody Category b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID de la categoria deben coincidir");
        return categoryService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Category getCategory(@PathVariable Long id){
		return categoryService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Category> getCategories(){
		return categoryService.getCategories();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		categoryService.delete(id);
	}
	
}
