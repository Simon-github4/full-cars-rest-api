package com.fullcars.restapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fullcars.restapi.dto.CustomerSummaryDTO;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.service.CustomerService;
import com.fullcars.restapi.service.PayService;
import com.fullcars.restapi.service.SaleService;

@RestController
@RequestMapping(value = "/customers")
public class CustomerController {

	private final CustomerService customerService;
	
	public CustomerController(CustomerService repo) {
		this.customerService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Customer post(@RequestBody Customer b) {
		return customerService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Customer put(@PathVariable Long id, @RequestBody Customer b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID del cliente deben coincidir");
        return customerService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Customer getCustomerById(@PathVariable Long id){
		return customerService.findByIdOrThrow(id);
	}
	
	@GetMapping(params = "dni")
	@ResponseStatus(HttpStatus.OK)
	public Customer getCustomerByDni(@RequestParam String dni){
		return customerService.findByDniOrThrow(dni);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Customer> getAllCustomers(){
		return customerService.getCustomers();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		customerService.delete(id);
	}

	@GetMapping("/{id}/summary")
	@ResponseStatus(HttpStatus.OK)
	public CustomerSummaryDTO getCustomerSummaryById(@PathVariable Long id){
		return customerService.getCustomerSummary(id);
	}
	
}