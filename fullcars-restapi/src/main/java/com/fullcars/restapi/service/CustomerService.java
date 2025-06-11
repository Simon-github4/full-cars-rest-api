package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.repository.ICustomerRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CustomerService {
	
	private ICustomerRepository customerRepo;
	
	public CustomerService(ICustomerRepository repo) {
		this.customerRepo = repo;
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
