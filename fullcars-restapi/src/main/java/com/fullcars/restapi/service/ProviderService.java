package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fullcars.restapi.event.PurchaseEvent;
import com.fullcars.restapi.model.Provider;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.repository.IProviderRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProviderService {

	private IProviderRepository providerRepo;
	
	public ProviderService(IProviderRepository repo) {
		this.providerRepo = repo;
	}
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onApplicationEvent(PurchaseEvent e) {
		Purchase sale = e.getEntity();
		System.err.println("PurchaseEvent REceived!!!; ProviderService" + e.getSource());
	}
	
	public Provider save(Provider c) {
		return providerRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!providerRepo.existsById(id)) 
            throw new EntityNotFoundException("Proveedor no encontrada con id: " + id);
        providerRepo.deleteById(id);
	}
	
	public Provider findByIdOrThrow(Long id) {
		return providerRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Proveedor no encontrada con id: " + id));
	}
	
	public List<Provider> getCategories(){
		return providerRepo.findAll();
	}
}
