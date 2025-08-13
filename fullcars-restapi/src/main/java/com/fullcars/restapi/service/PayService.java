package com.fullcars.restapi.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.repository.IPayRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PayService {
	
	private IPayRepository payRepo;
	
	public PayService(IPayRepository repo) {
		this.payRepo = repo;
	}
	
	public Pay save(Pay c) {
		return payRepo.save(c);
	}
	
	public void delete(Long id) {
        if (!payRepo.existsById(id)) 
            throw new EntityNotFoundException("Pago no encontrado con id: " + id);
        payRepo.deleteById(id);
	}
	
	public Pay findByIdOrThrow(Long id) {
		return payRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Pago no encontrado con id: " + id));
	}
	
	public List<Pay> getPayments(){
		return payRepo.findAll();
	}

	public List<Pay> getPayments(Long customerId) {
		return payRepo.findByCustomerId(customerId);
	}
}