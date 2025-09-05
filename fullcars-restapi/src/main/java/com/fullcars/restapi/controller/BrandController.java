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

import com.fullcars.restapi.model.Brand;
import com.fullcars.restapi.service.BrandService;


@RestController
@RequestMapping(value = "/brands")
public class BrandController {

	private BrandService brandService;
	
	public BrandController(BrandService repo) {
		this.brandService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Brand postBrand(@RequestBody Brand b) throws ServerException {
		return brandService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Brand putBrand(@PathVariable Long id, @RequestBody Brand b) throws ServerException {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("Path ID and brand ID must match.");
        return brandService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Brand getBrand(@PathVariable Long id){
		return brandService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Brand> getBrands(){
		return brandService.getBrands();
	}
	
	@DeleteMapping("/{id}")
	public void deleteBrand(@PathVariable Long id) {
		brandService.delete(id);
	}
}
