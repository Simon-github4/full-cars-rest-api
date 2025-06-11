package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.model.Brand;
import com.fullcars.restapi.repository.IBrandRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BrandService {

	private IBrandRepository brandRepo;
	
	public BrandService(IBrandRepository repo) {
		this.brandRepo = repo;
	}
	
	@Transactional
	public Brand save(Brand b) {
		return brandRepo.save(b);
	}
	
	@Transactional
	public void delete(Long id) {
        if (!brandRepo.existsById(id)) 
            throw new EntityNotFoundException("Marca no encontrada con id: " + id);
        brandRepo.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public Brand findByIdOrThrow(Long id) {
		return brandRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Marca no encontrada con id: " + id));
	}
	@Transactional(readOnly = true)
	public List<Brand> getBrands(){
		return brandRepo.findAll();
	}
	
}
