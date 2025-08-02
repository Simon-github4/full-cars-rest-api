package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.model.Provider;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.repository.IPurchaseRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PurchaseService {

	private IPurchaseRepository purchaseRepo;
	private ProviderService providerService;
	
	public PurchaseService(IPurchaseRepository repo, ProviderService providerService) {
		this.purchaseRepo = repo;
		this.providerService = providerService;
	}
	
	public Purchase save(Purchase p, Long idProvider) {
		Provider c = providerService.findByIdOrThrow(idProvider);
		p.setProvider(c);
		p.setAdressSnapshot(c.getAdress());
		p.setCompanyNameSnapshot(c.getCompanyName());
		p.setCuitSnapshot(c.getCuit());
		return purchaseRepo.save(p);
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
