package com.fullcars.restapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.enums.MovementType;
import com.fullcars.restapi.event.PurchaseDetailEvent;
import com.fullcars.restapi.event.SaleDetailEvent;
import com.fullcars.restapi.event.StockMovementEvent;
import com.fullcars.restapi.model.PurchaseDetail;
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
		System.err.println("SaleDetailEvent REceived!!!" + e.getSource());
		SaleDetail detail = e.getEntity();
		if(e.getEventType() == EventType.INSERT) {
			StockMovement move = StockMovement.builder()
					.id(null)
					.carPart(detail.getProduct())
					.quantity(detail.getQuantity())
					.date(detail.getSale().getDate())
					.reference("Venta "+ detail.getSale().getDate().toString() +", "+ detail.getSale().getCustomer())
					.type(MovementType.SALIDA_VENTA)
					.saleDetail(detail)
					.build();
			save(move);
		}else if(e.getEventType() == EventType.DELETE) 
			deleteByDetail(detail);
	}
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleSaleEvent(PurchaseDetailEvent e) {
		System.err.println("PurchaseDetailEvent REceived!!!" + e.getSource());
		PurchaseDetail detail = e.getEntity();
		if(e.getEventType() == EventType.INSERT) {
			StockMovement move = StockMovement.builder()
					.id(null)
					.carPart(detail.getProduct())
					.date(detail.getPurchase().getDate())
					.quantity(detail.getQuantity())
					.reference("Compra "+ detail.getPurchase().getDate().toString() +", "+ detail.getPurchase().getProvider())
					.type(MovementType.ENTRADA_COMPRA)
					.purchaseDetail(detail)
					.build();
			save(move);
		}else if(e.getEventType() == EventType.DELETE) 
			deleteByDetail(detail);
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
        StockMovement move = stockRepo.findByPurchaseDetail(d).orElseThrow(()-> 
					new EntityNotFoundException("Movimiento no encontrado")); 
        stockRepo.delete(move);
        appEventPublisher.publishEvent(new StockMovementEvent(this, move, EventType.DELETE, getCurrentStockByCarPartId(move.getCarPart().getId())));
	}
	@Transactional
	public void deleteByDetail(SaleDetail d) {
        StockMovement move = stockRepo.findBySaleDetail(d).orElseThrow(()-> 
					new EntityNotFoundException("Movimiento no encontrado")); 
        stockRepo.delete(move);
        appEventPublisher.publishEvent(new StockMovementEvent(this, move, EventType.DELETE, getCurrentStockByCarPartId(move.getCarPart().getId())));
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
	
}
