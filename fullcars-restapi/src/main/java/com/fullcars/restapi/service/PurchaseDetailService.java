package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.model.PurchaseDetail;
import com.fullcars.restapi.repository.IPurchaseDetailRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PurchaseDetailService {

	private IPurchaseDetailRepository detailRepo;
	
	public PurchaseDetailService(IPurchaseDetailRepository repo) {
		this.detailRepo = repo;
	}
	
	@Transactional(readOnly = true)
	public PurchaseDetail save(PurchaseDetail c) {
		return detailRepo.save(c);
	}
	
	@Transactional(readOnly = true)
	public void delete(Long id) {
        if (!detailRepo.existsById(id)) 
            throw new EntityNotFoundException("Detalle no encontrado con id: " + id);
        detailRepo.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public PurchaseDetail findByIdOrThrow(Long id) {
		return detailRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Detalle no encontrada con id: " + id));
	}
	
	@Transactional(readOnly = true)
	public List<PurchaseDetail> getDetails(){
		return detailRepo.findAll();
	}
}
