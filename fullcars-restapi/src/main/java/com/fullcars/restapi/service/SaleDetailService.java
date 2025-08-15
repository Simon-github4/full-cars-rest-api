package com.fullcars.restapi.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.model.SaleDetail;
import com.fullcars.restapi.repository.ISaleDetailRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SaleDetailService {// NOT USED
	
	private ISaleDetailRepository detailRepo;
	
	public SaleDetailService(ISaleDetailRepository repo) {
		this.detailRepo = repo;
	}
	
	public SaleDetail save(SaleDetail c) {
		return detailRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!detailRepo.existsById(id)) 
            throw new EntityNotFoundException("Detalle no encontrado con id: " + id);
        detailRepo.deleteById(id);
	}
	
    @Transactional(readOnly = true)
	public SaleDetail findByIdOrThrow(Long id) {
		return detailRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Detalle no encontrada con id: " + id));
	}
	
    @Transactional(readOnly = true)
	public List<SaleDetail> getDetails(){
		return detailRepo.findAll();
	}
}