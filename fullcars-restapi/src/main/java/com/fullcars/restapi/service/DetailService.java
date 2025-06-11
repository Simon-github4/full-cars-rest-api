package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.fullcars.restapi.model.Detail;
import com.fullcars.restapi.repository.IDetailRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DetailService {
	
	private IDetailRepository detailRepo;
	
	public DetailService(IDetailRepository repo) {
		this.detailRepo = repo;
	}
	
	public Detail save(Detail c) {
		return detailRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!detailRepo.existsById(id)) 
            throw new EntityNotFoundException("Detalle no encontrado con id: " + id);
        detailRepo.deleteById(id);
	}
	
	public Detail findByIdOrThrow(Long id) {
		return detailRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Detalle no encontrada con id: " + id));
	}
	
	public List<Detail> getDetails(){
		return detailRepo.findAll();
	}
}