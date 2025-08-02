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

import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.model.SaleDetail;
import com.fullcars.restapi.service.SaleDetailService;
import com.fullcars.restapi.service.SaleService;

@RestController
@RequestMapping(value = "/sales")
public class SaleController {

private SaleService saleService;
private SaleDetailService detailsService;
	
	public SaleController(SaleService repo, SaleDetailService repod) {
		this.saleService = repo;
		this.detailsService = repod;
	}
	
	@GetMapping(value = "/event")
	@ResponseStatus(HttpStatus.OK)
	public void event() {
		saleService.save(new Sale(), 1L);
		detailsService.save(new SaleDetail());
	}
	
	@PostMapping("/{idCustomer}")
	@ResponseStatus(HttpStatus.CREATED)
	public Sale post(@RequestBody Sale sale, @PathVariable Long idCustomer) {
		sale.getDetails().forEach(d -> d.setSale(sale));
		return saleService.save(sale, idCustomer);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Sale getSale(@PathVariable Long id){
		return saleService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Sale> getSales(){
		return saleService.getSales();
	}
	
	@GetMapping("/filters")
	@ResponseStatus(HttpStatus.OK)
	public List<Sale> getSalesFiltered(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
		    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
		    @RequestParam(required = false) Long idCustomer) {
		return saleService.getSales(start, end, idCustomer);
	}	
	
	@DeleteMapping("/{id}")
	public void deleteSale(@PathVariable Long id) {
		saleService.delete(id);
	}
	
//------------------------------------------- Details ------------------------------------------------------
	@PostMapping("/{id}/details")
	@ResponseStatus(HttpStatus.CREATED)
	public SaleDetail postSaleDetail(@PathVariable Long id, @RequestBody SaleDetail b) {
		return (SaleDetail) detailsService.save(b);
	}

	@PutMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public SaleDetail put(@PathVariable Long id, @RequestBody SaleDetail b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID del detalle deben coincidir");
        return detailsService.save(b);
    }
	
	@GetMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public SaleDetail getSaleDetail(@PathVariable Long id){
		return detailsService.findByIdOrThrow(id);
	}

	@DeleteMapping("/details/{id}")
	public void deleteDetail(@PathVariable Long id) {
		detailsService.delete(id);
	}
	
	/*@GetMapping("/{id}/details")   innecesario, getSales los devuelve
	@ResponseStatus(HttpStatus.OK)
	public List<SaleDetail> getDetails(){
		return detailsService.getDetails();
	}*/
	
	
}
