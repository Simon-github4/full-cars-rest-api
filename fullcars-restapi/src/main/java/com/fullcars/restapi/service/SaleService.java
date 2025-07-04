package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ISaleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SaleService {

	private final ISaleRepository saleRepo;
	private final ApplicationEventPublisher appEventPublisher;
	
	@Autowired
	public SaleService(ISaleRepository repo, ApplicationEventPublisher publisher) {
		this.saleRepo = repo;
		this.appEventPublisher = publisher;
	}
	
	@Transactional
	public Sale save(Sale b) {
		Sale sale = saleRepo.save(b);
		appEventPublisher.publishEvent(new SaleEvent(this, sale, EventType.INSERT));
		return sale;
	}
	
	@Transactional
	public void delete(Long id) {
        if (!saleRepo.existsById(id)) 
            throw new EntityNotFoundException("Venta no encontrada con id: " + id);
        saleRepo.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public Sale findByIdOrThrow(Long id) {
		return saleRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Venta no encontrada con id: " + id));
	}
	
	@Transactional(readOnly = true)
	public List<Sale> getSales(){
		return saleRepo.findAll();
	}
	
}
