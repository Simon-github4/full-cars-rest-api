package com.fullcars.restapi.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.PurchaseEvent;
import com.fullcars.restapi.model.Provider;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.repository.IPurchaseRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PurchaseService {

	private final IPurchaseRepository purchaseRepo;
	private final ProviderService providerService;
	private final ApplicationEventPublisher appEventPublisher;

	public PurchaseService(IPurchaseRepository repo, ProviderService providerService, ApplicationEventPublisher appEventPublisher) {
		this.purchaseRepo = repo;
		this.providerService = providerService;
		this.appEventPublisher = appEventPublisher;
	}
	
	@Transactional
	public Purchase save(Purchase p, Long idProvider) {
		p.setProvider(providerService.findByIdOrThrow(idProvider));
		p.getDetails().forEach(d -> d.setPurchase(p));
		Purchase savedPurchase = purchaseRepo.save(p);
		appEventPublisher.publishEvent(new PurchaseEvent(this, savedPurchase, EventType.INSERT));
		return savedPurchase;
	}
	
	@Transactional
	public void delete(Long id) {
        Purchase deletedPurchase = purchaseRepo.findById(id).orElseThrow(()-> 
        							new EntityNotFoundException("Compra no encontrado con id: " + id)); 
        purchaseRepo.deleteById(id);
        appEventPublisher.publishEvent(new PurchaseEvent(this, deletedPurchase, EventType.DELETE));
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

	@Transactional(readOnly = true)
	public List<Purchase> getPurchases(LocalDate start, LocalDate end, Long idProvider) {
		if (start != null && end != null && idProvider != null) 
	        return purchaseRepo.findByDateBetweenAndProviderId(start, end, idProvider);
	     else if (start != null && end != null) 
	        return purchaseRepo.findByDateBetween(start, end);
	     else if (idProvider != null) 
	        return purchaseRepo.findByProviderId(idProvider);
	     else 
	        return purchaseRepo.findAll();
	}
	
}
