package com.fullcars.restapi.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.model.PurchaseDetail;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.service.PurchaseDetailService;
import com.fullcars.restapi.service.PurchaseService;

@RestController
@RequestMapping(value = "/purchases")
public class PurchaseController {

	private final PurchaseService purchaseService;
	private final PurchaseDetailService detailsService;

	public PurchaseController(PurchaseService repo, PurchaseDetailService repod) {
		this.purchaseService = repo;
		this.detailsService = repod;
	}
	
	@PostMapping("/{idProvider}")
	@ResponseStatus(HttpStatus.CREATED)
	public Purchase post(@RequestBody Purchase b, @PathVariable Long idProvider) {
		return purchaseService.save(b, idProvider);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Purchase getPurchase(@PathVariable Long id){
		return purchaseService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Purchase> getPurchases(){
		return purchaseService.getPurchases();
	}
	
	@GetMapping("/filters")
	@ResponseStatus(HttpStatus.OK)
	public List<Purchase> getSalesFiltered(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
		    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
		    @RequestParam(required = false) Long idProvider) {
		return purchaseService.getPurchases(start, end, idProvider);
	}	
	
	@DeleteMapping("/{id}")
	public void deletePurchase(@PathVariable Long id) {
		purchaseService.delete(id);
	}
	
//----------------------------------------------- Details ----------------------------------------
	@PostMapping("/{id}/details")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseDetail postPurchaseDetail(@RequestBody PurchaseDetail b) {
		return (PurchaseDetail)detailsService.save(b);
	}
	
	//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public PurchaseDetail put(@PathVariable Long id, @RequestBody PurchaseDetail b) {
		if (!id.equals(b.getId())) 
			throw new IllegalArgumentException("El ID enviado y el ID del detalle deben coincidir");
	        return detailsService.save(b);
	    }
		
	@GetMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public PurchaseDetail getPurchaseDetail(@PathVariable Long id){
		return detailsService.findByIdOrThrow(id);
	}
	
	/*@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<PurchaseDetail> getDetails(){
		return detailsService.getDetails();
	}*/
	
	@DeleteMapping("/details/{id}")
	public void deleteDetail(@PathVariable Long id) {
		detailsService.delete(id);
	}
	
}