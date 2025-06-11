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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.service.PayService;

@RestController
@RequestMapping(value = "/payments")
public class PayController {

private PayService payService;
	
	public PayController(PayService repo) {
		this.payService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Pay post(@RequestBody Pay b) {
		return payService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Pay put(@PathVariable Long id, @RequestBody Pay b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID del pago deben coincidir");
        return payService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Pay getCategory(@PathVariable Long id){
		return payService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Pay> getPayments(){
		return payService.getPayments();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		payService.delete(id);
	}
	
}

