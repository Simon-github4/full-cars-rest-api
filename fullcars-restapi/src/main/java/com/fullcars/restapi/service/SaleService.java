package com.fullcars.restapi.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ISaleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SaleService {

	private final ISaleRepository saleRepo;
	private final CustomerService customerService;
	private final ApplicationEventPublisher appEventPublisher;
	
	public SaleService(ISaleRepository repo, ApplicationEventPublisher publisher, CustomerService customerService) {
		this.saleRepo = repo;
		this.appEventPublisher = publisher;
		this.customerService = customerService;
	}
	
	@Transactional
	public Sale save(Sale sale, Long idCustomer) {
		Customer c = customerService.findByIdOrThrow(idCustomer);
		sale.setCustomer(c);
		sale.getDetails().forEach(d -> d.setSale(sale));
		Sale savedSale = saleRepo.save(sale);
		appEventPublisher.publishEvent(new SaleEvent(this, savedSale, EventType.INSERT));
		return savedSale;
	}
	
	@Transactional
	public void delete(Long id) {
        Sale sale = saleRepo.findById(id).orElseThrow(() -> 
					new EntityNotFoundException("Venta no encontrada con id: " + id));			 
        saleRepo.deleteById(id);
        appEventPublisher.publishEvent(new SaleEvent(this, sale, EventType.DELETE));
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

	@Transactional(readOnly = true)
	public List<Sale> getSales(LocalDate start, LocalDate end, Long idCustomer) {
		if (start != null && end != null && idCustomer != null) {
	        return saleRepo.findByDateBetweenAndCustomerId(start, end, idCustomer);
	    } else if (start != null && end != null) {
	        return saleRepo.findByDateBetween(start, end);
	    } else if (idCustomer != null) {
	        return saleRepo.findByCustomerId(idCustomer);
	    } else {
	        return saleRepo.findAll();
	    }
	}
	
}
