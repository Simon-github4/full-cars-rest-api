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

import com.fullcars.restapi.model.Provider;
import com.fullcars.restapi.service.ProviderService;

@RestController
@RequestMapping(value = "/providers")
public class ProviderController {

private ProviderService providerService;
	
	public ProviderController(ProviderService repo) {
		this.providerService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Provider post(@RequestBody Provider b) {
		return providerService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Provider put(@PathVariable Long id, @RequestBody Provider b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID de la categoria deben coincidir");
        return providerService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Provider getCategory(@PathVariable Long id){
		return providerService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Provider> getCategories(){
		return providerService.getCategories();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		providerService.delete(id);
	}
	
}