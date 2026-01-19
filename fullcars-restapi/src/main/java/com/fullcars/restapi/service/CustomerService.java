package com.fullcars.restapi.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fullcars.restapi.dto.CustomerSummaryDTO;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.repository.ICustomerRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CustomerService {
	
	private final ICustomerRepository customerRepo;
	private final PayService payService;
	private final SaleService saleService;
	
	public CustomerService(ICustomerRepository repo, PayService payService, @Lazy SaleService saleService) {
		this.customerRepo = repo;
		this.payService = payService;
		this.saleService = saleService;
	}
	
	public Customer save(Customer c) {
		return customerRepo.save(c);
	}
	
	public void delete(Long id) {
        customerRepo.deleteById(id);
	}
	
	public Customer findByIdOrThrow(Long id) {
		return customerRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Cliente no encontrada con id: " + id));
	}
	
	public List<Customer> getCustomers(){
		return customerRepo.findAll(Sort.by(Sort.Direction.ASC, "fullName"));
	}

	public Customer findByDniOrThrow(String dni) {
		return customerRepo.findByDni(dni).orElseThrow(() -> 
				new EntityNotFoundException("Cleinte no encontrado con dni: " + dni));
	}

	public CustomerSummaryDTO getCustomerSummary(Long customerId) {
		CustomerSummaryDTO summary = new CustomerSummaryDTO();
		summary.setCustomer(findByIdOrThrow(customerId));
		summary.setSales(saleService.getSales(null, null, customerId));
		summary.setPayments(payService.getPayments(customerId));
		return summary;
	}

	public BigDecimal calculateTotalPayments() {
		return customerRepo.calculateTotalPayments();
	}
}
