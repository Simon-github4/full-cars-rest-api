package com.fullcars.restapi.controller;

import java.time.LocalDate;
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

import com.fullcars.restapi.enums.MovementType;
import com.fullcars.restapi.model.StockMovement;
import com.fullcars.restapi.service.StockMovementService;

@RestController
@RequestMapping(value = "/stockmovements")
public class StockMovementController {
	
	private StockMovementService stockService;
	
	public StockMovementController(StockMovementService service) {
		this.stockService = service;
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<StockMovement> getStockMovements(){
		return stockService.getStockMovements();
	}

	@GetMapping(params = {"start", "end"})
	public List<StockMovement> getStockMovementsBetweenDates(
	        @RequestParam LocalDate start,
	        @RequestParam LocalDate end) {
	    return stockService.getStockMovementsBetween(start, end);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public StockMovement getStockMovement(@PathVariable Long id){
		return stockService.findByIdOrThrow(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public StockMovement post(@RequestBody StockMovement b) {
		if(b.getId() == null && (b.getType() == MovementType.ENTRADA_COMPRA || b.getType() == MovementType.SALIDA_VENTA))
			throw new IllegalArgumentException("El tipo de Movimiento tiene que ser de Ajuste");
		return stockService.save(b);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public StockMovement put(@PathVariable Long id, @RequestBody StockMovement b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID del Movimiento deben coincidir");
        return stockService.save(b);
    }
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		stockService.delete(id);
	}
	
}
