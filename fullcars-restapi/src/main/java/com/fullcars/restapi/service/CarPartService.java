package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
	
	@Transactional  //still extends same transaction of invoker
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
		if (c.getId() != null){//update 
			c.setStock(findByIdOrThrow(c.getId()).getStock());//ensure stock is not modified on the client
			c.setSku(generateSku(c));
			return carPartRepo.save(c);
		}
		else {//insert
			CarPart saved = carPartRepo.save(c);
			saved.setSku(generateSku(saved));
			carPartRepo.save(saved);
			return saved;
		} 
	}

	public void delete(Long id) {
        if (!carPartRepo.existsById(id)) 
            throw new EntityNotFoundException("Auto Parte no encontrada con id: " + id);
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
		/*sb.append(getSafePrefix(c.getBrand().getName())).append("-")
		  .append(getSafePrefix(c.getCategory().getName())).append("-")
		  .append(getSafePrefix(c.getName())).append("-")
		  .append(c.getId().toString());*/
		sb.append(c.getBrand().getId().toString()).append("-")
		  .append(c.getCategory().getId().toString()).append("-")
		  .append(c.getProvider().getId().toString()).append("-")
		  .append(c.getId().toString());
		return sb.toString();
	}
	private String getSafePrefix(String value) {
        return value == null ? "XXX" : value.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, value.length()));
    }
}
