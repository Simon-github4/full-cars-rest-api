package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.enums.MovementType;
import com.fullcars.restapi.event.PurchaseDetailEvent;
import com.fullcars.restapi.event.PurchaseEvent;
import com.fullcars.restapi.event.SaleDetailEvent;
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

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleSaleEvent(SaleDetailEvent e) {
		SaleDetail sale = e.getEntity();
		System.err.println("SaleDetailEvent REceived!!!" + e.getSource());
	}
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleSaleEvent(PurchaseDetailEvent e) {
		PurchaseDetail sale = e.getEntity();
		System.err.println("PurchaseDetailEvent REceived!!!" + e.getSource());
	}
	
	@Transactional
	public StockMovement save(StockMovement sm) {
		StockMovement saved = stockRepo.save(sm);
		appEventPublisher.publishEvent(new StockMovementEvent(this, saved, EventType.INSERT));
		return saved;
	}
	
	@Transactional
	public void delete(Long id) {
        if (!stockRepo.existsById(id)) 
            throw new EntityNotFoundException("Movimiento no encontrada con id: " + id);
        stockRepo.deleteById(id);
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
	
}
