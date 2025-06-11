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

import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.service.CarPartService;

@RestController
@RequestMapping(value = "/carparts")
public class CarPartController {

	private CarPartService carPartService;
	
	public CarPartController(CarPartService repo) {
		this.carPartService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CarPart post(@RequestBody CarPart b) {
		return carPartService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public CarPart put(@PathVariable Long id, @RequestBody CarPart b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID de la Auto Parte deben coincidir");
        return carPartService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public CarPart getCategory(@PathVariable Long id){
		return carPartService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<CarPart> getCarParts(){
		return carPartService.getCarParts();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		carPartService.delete(id);
	}
	
}
