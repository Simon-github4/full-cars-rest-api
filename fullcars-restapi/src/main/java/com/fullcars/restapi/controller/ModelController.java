package com.fullcars.restapi.controller;

import java.rmi.ServerException;
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
import com.fullcars.restapi.model.Model;
import com.fullcars.restapi.service.ModelService;

@RestController
@RequestMapping("/models")
public class ModelController {

private ModelService modelService;
	
	public ModelController(ModelService repo) {
		this.modelService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Model postBrand(@RequestBody Model b) throws ServerException {
		return modelService.save(b);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Model getModel(@PathVariable Long id){
		return modelService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Model> getModels(){
		return modelService.getModels();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		modelService.delete(id);
	}
}
