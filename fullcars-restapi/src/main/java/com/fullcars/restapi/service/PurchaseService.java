package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.repository.IPurchaseRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PurchaseService {

	private IPurchaseRepository purchaseRepo;
	
	public PurchaseService(IPurchaseRepository repo) {
		this.purchaseRepo = repo;
	}
	
	public Purchase save(Purchase c) {
		return purchaseRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!purchaseRepo.existsById(id)) 
            throw new EntityNotFoundException("Compra no encontrado con id: " + id);
        purchaseRepo.deleteById(id);
	}
	
    @Transactional(readOnly = true)
	public Purchase findByIdOrThrow(Long id) {
		return purchaseRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Compra no encontrada con id: " + id));
	}
	
    @Transactional(readOnly = true)
	public List<Purchase> getPurchases(){
		return purchaseRepo.findAll();
	}
}
