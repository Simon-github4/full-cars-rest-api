package com.fullcars.restapi.service;

import java.rmi.ServerException;
import java.util.List;

import org.springframework.data.domain.Sort;
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
	public Brand save(Brand b) throws ServerException {
		if(b.getId() == null && findByName(b.getName()) != null)
			throw new ServerException("Ya Existe una Marca con ese nombre");
		return brandRepo.save(b);
	}
	@Transactional(readOnly = true)
	public Brand findByName(String name) {
		return brandRepo.findByName(name);
	}
	
	@Transactional
	public void delete(Long id) {
        brandRepo.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public Brand findByIdOrThrow(Long id) {
		return brandRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Marca no encontrada con id: " + id));
	}
	@Transactional(readOnly = true)
	public List<Brand> getBrands(){
		return brandRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
}
