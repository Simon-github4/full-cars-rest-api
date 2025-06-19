package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.repository.ICarPartRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CarPartService {

private ICarPartRepository carPartRepo;
	
	public CarPartService(ICarPartRepository repo) {
		this.carPartRepo = repo;
	}
	
	public CarPart save(CarPart c) {
		return carPartRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!carPartRepo.existsById(id)) 
            throw new EntityNotFoundException("Auto Parte no encontrada con id: " + id);
        carPartRepo.deleteById(id);
	}
	
	public CarPart findByIdOrThrow(Long id) {
		return carPartRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Auto Parte no encontrada con id: " + id));
	}
	
	public CarPart findBySku(String sku) {
		return carPartRepo.findBySku(sku).orElseThrow(() -> 
						new EntityNotFoundException("Auto Parte no encontrada con sku: " + sku));
	}
	
	public List<CarPart> getCarParts(){
		return carPartRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}
}
