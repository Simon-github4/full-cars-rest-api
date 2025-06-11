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

import com.fullcars.restapi.model.Detail;
import com.fullcars.restapi.model.PurchaseDetail;
import com.fullcars.restapi.model.SaleDetail;
import com.fullcars.restapi.service.DetailService;

@RestController
@RequestMapping(value = "/details")
public class DetailController {

private DetailService detailsService;
	
	public DetailController(DetailService repo) {
		this.detailsService = repo;
	}
	
	@PostMapping("/sale")
	@ResponseStatus(HttpStatus.CREATED)
	public SaleDetail postSaleDetail(@RequestBody SaleDetail b) {
		return (SaleDetail) detailsService.save(b);
	}
	
	@PostMapping("/purchase")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseDetail postPurchaseDetail(@RequestBody PurchaseDetail b) {
		return (PurchaseDetail)detailsService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Detail put(@PathVariable Long id, @RequestBody Detail b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID del detalle deben coincidir");
        return detailsService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Detail getCategory(@PathVariable Long id){
		return detailsService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Detail> getDetails(){
		return detailsService.getDetails();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		detailsService.delete(id);
	}
	
}

