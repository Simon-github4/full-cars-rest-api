package com.fullcars.restapi.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.enums.MovementType;
import com.fullcars.restapi.event.PurchaseEvent;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.event.StockMovementEvent;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.model.PurchaseDetail;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.model.SaleDetail;
import com.fullcars.restapi.model.StockMovement;
import com.fullcars.restapi.repository.IStockMovementRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class StockMovementService {//implements ApplicationListener<SaleEvent>{
									//@Async
	@Autowired
	private IStockMovementRepository stockRepo;
	@Autowired
	private ApplicationEventPublisher appEventPublisher;
	
	@Transactional
	@EventListener
	public void handleSaleEvent(SaleEvent e) {
		System.err.println("SaleEvent REceived!!!" + e.getSource());
		Sale sale = e.getEntity();
		if(e.getEventType() == EventType.INSERT) {
			sale.getDetails().forEach(detail ->{
				StockMovement move = StockMovement.builder()
					.id(null)
					.carPart(detail.getProduct())
					.quantity(detail.getQuantity())
					.date(detail.getSale().getDate())
					.reference("Venta "+ detail.getSale().getId())
					.type(MovementType.SALIDA_VENTA)
					.saleDetail(detail)
					.build();
				save(move);
			});
		}else if(e.getEventType() == EventType.DELETE) 
			sale.getDetails().forEach(detail -> this.deleteByDetail(detail));
	}

	@Transactional//EventListener(phase = TransactionPhase.AFTER_COMMIT)
	@EventListener
	public void handlePurchaseEvent(PurchaseEvent e) {
		System.err.println("PurchaseEvent REceived!!!" + e.getEntity().getDate().toString());
		Purchase purchase = e.getEntity();
		if(e.getEventType() == EventType.INSERT) {
			purchase.getDetails().forEach(detail -> {				
				StockMovement move = StockMovement.builder()
						.id(null)
						.carPart(detail.getProduct())
						.date(detail.getPurchase().getDate())
						.quantity(detail.getQuantity())
						.reference("Compra "+ detail.getPurchase().getId())
						.type(MovementType.ENTRADA_COMPRA)
						.purchaseDetail(detail)
						.build();
				save(move);
			});
		}else if(e.getEventType() == EventType.DELETE) 
			purchase.getDetails().forEach(detail -> this.deleteByDetail(detail));
	}	
	
	@Transactional
	public StockMovement save(StockMovement sm) {
		StockMovement saved = stockRepo.save(sm);
		appEventPublisher.publishEvent(new StockMovementEvent(this, saved, EventType.INSERT, getCurrentStockByCarPartId(saved.getCarPart().getId())));
		return saved;
	}
	
	@Transactional
	public void delete(Long id) {
        StockMovement move = stockRepo.findById(id).orElseThrow(()-> 
        			new EntityNotFoundException("Movimiento no encontrada con id: " + id)); 
        stockRepo.deleteById(id);
		appEventPublisher.publishEvent(new StockMovementEvent(this, move, EventType.DELETE, getCurrentStockByCarPartId(move.getCarPart().getId())));
	}

	@Transactional
	public void deleteByDetail(PurchaseDetail d) {
        stockRepo.findByPurchaseDetail(d).ifPresent(move -> {
	        stockRepo.delete(move);
	        appEventPublisher.publishEvent(
	            new StockMovementEvent(this, move, EventType.DELETE, getCurrentStockByCarPartId(move.getCarPart().getId()))
	        );
	    });
	}
	@Transactional
	public void deleteByDetail(SaleDetail d) {
	    stockRepo.findBySaleDetail(d).ifPresent(move -> {
	        stockRepo.delete(move);
	        appEventPublisher.publishEvent(
	            new StockMovementEvent(this, move, EventType.DELETE, getCurrentStockByCarPartId(move.getCarPart().getId()))
	        );
	    });
	}
	
	@Transactional(readOnly = true)
	public StockMovement findByIdOrThrow(Long id) {
		return stockRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Movimiento no encontrada con id: " + id));
	}
	
	@Transactional(readOnly = true)
	public List<StockMovement> getStockMovements(){
		return stockRepo.findAll();
	}
	
	public Long getCurrentStockByCarPartId(Long id) {
		Long stock = stockRepo.getCurrentStockByCarPartId(id);
		return stock != null ? stock : 0;
	}

	public List<StockMovement> getStockMovementsBetween(LocalDate start, LocalDate end) {
		return stockRepo.findByDateBetween(start, end);
	}
	
}
