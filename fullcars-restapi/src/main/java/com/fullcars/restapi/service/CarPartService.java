package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.dto.TopProductDTO;
import com.fullcars.restapi.event.StockMovementEvent;
import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.repository.ICarPartRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CarPartService {

private ICarPartRepository carPartRepo;
	
	public CarPartService(ICarPartRepository repo) {
		this.carPartRepo = repo;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	@EventListener  //on the same Transaction of the methos that publish it  ; TransactionalEventListener(AFTER_COMMIT) is when the Transaction of the invoker finish 
    public void handleStockMovementEvent(StockMovementEvent event) {
	    Long carPartId = event.getEntity().getCarPart().getId();
        Long currentStock = event.getCurrentStock();

        carPartRepo.findById(carPartId).ifPresent(carPart -> {
            carPart.setStock(currentStock); 
            carPartRepo.save(carPart);
        });
    }
	
	@Transactional
	public CarPart save(CarPart c) {
		if (c.getId() != null){			
			c.setStock(findByIdOrThrow(c.getId()).getStock());//ensure stock is not modified on the client
			c.setSku(generateSku(c));
			return carPartRepo.save(c);
		}
		else {
			CarPart saved = carPartRepo.save(c);
			saved.setSku(generateSku(saved));
			carPartRepo.save(saved);
			return saved;
		} 
	}

	public void delete(Long id) {
        carPartRepo.deleteById(id);
	}
	
	public CarPart findByIdOrThrow(Long id) {
		return carPartRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Auto Parte no encontrada con id: " + id));
	}
	
	public CarPart findBySku(String sku) {
		return carPartRepo.findBySku(sku).orElseThrow(() -> 
						new EntityNotFoundException("Auto Parte no encontrada con sku: " + sku));
	}
	
	@Transactional(readOnly = true)
	public List<CarPart> getCarParts(){
		return carPartRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}
	
	private String generateSku(CarPart c) {
		StringBuilder sb = new StringBuilder();
		sb.append(c.getModel().getBrand().getId().toString()).append("-")
		  .append(c.getCategory().getId().toString()).append("-")
		  //.append(c.getProvider().getId().toString()).append("-")
		  .append(c.getId().toString());
		return sb.toString();
	}
	private String getSafePrefix(String value) {
        return value == null ? "XXX" : value.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, value.length()));
    }

	public List<CarPart> getCriticalStock() {
		return carPartRepo.findByStockLessThan(5L);
	}

	public long getRegisteredCarParts() {
		return carPartRepo.count();
	}
	public List<TopProductDTO> getTopProducts(int limit) {
	    return carPartRepo.findTopProducts(PageRequest.of(0, limit));
	}

}
