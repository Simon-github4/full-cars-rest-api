package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.repository.ICarPartRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CarPartService {

private ICarPartRepository carPartRepo;
	
	public CarPartService(ICarPartRepository repo) {
		this.carPartRepo = repo;
	}
	
	@Transactional
	public CarPart save(CarPart c) {
		CarPart part = carPartRepo.save(c);
		
		if (part.getSku() == null || part.getSku().isBlank()) { 
            part.setSku(generateSku(part));
			part = carPartRepo.save(part);
		}
		return part;
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
	
	@Transactional(readOnly = true)
	public List<CarPart> getCarParts(){
		return carPartRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}
	
	private String generateSku(CarPart c) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSafePrefix(c.getBrand().getName())).append("-")
		  .append(getSafePrefix(c.getCategory().getName())).append("-")
		  .append(getSafePrefix(c.getName())).append("-")
		  .append(c.getId().toString());
		return sb.toString();
	}
	private String getSafePrefix(String value) {
        return value == null ? "XXX" : value.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, value.length()));
    }
}
