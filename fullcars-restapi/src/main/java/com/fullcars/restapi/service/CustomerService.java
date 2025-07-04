package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fullcars.restapi.event.PayEvent;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ICustomerRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CustomerService {
	
	private ICustomerRepository customerRepo;
	
	public CustomerService(ICustomerRepository repo) {
		this.customerRepo = repo;
	}
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleSaleEvent(SaleEvent e) {
		Sale sale = e.getEntity();
		System.err.println("SaleEvent REceived!!!" + e.getSource());
	}
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePayEvent(PayEvent e) {
		Pay sale = e.getEntity();
		System.err.println("PayEvent REceived!!!" + e.getSource());
	}
	
	public Customer save(Customer c) {
		return customerRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!customerRepo.existsById(id)) 
            throw new EntityNotFoundException("Cliente no encontrada con id: " + id);
        customerRepo.deleteById(id);
	}
	
	public Customer findByIdOrThrow(Long id) {
		return customerRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Cliente no encontrada con id: " + id));
	}
	
	public List<Customer> getCustomers(){
		return customerRepo.findAll();
	}
}
