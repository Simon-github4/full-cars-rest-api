package com.fullcars.restapi.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.IFacturaRepository;

@Service
public class FacturaService {

	private final IFacturaRepository repo;
	
	public FacturaService(IFacturaRepository repo) {
		this.repo = repo;
	}
	
	@Transactional
	@EventListener
	public void handleSaleEvent(SaleEvent e) {
		System.err.println("Facuta Service Received SaleEvent !!!" + e.getSource());
		Sale sale = e.getEntity();
		if(e.getEventType().equals(EventType.DELETE))
			deleteBySaleId(sale.getId());
		else if(e.getEventType().equals(EventType.INSERT))
			save(sale);
	}
	
	@Transactional
	public Factura save(Sale sale) {
		Factura factura = new Factura();
		factura.setAdressSnapshot(sale.getCustomer().getAdress());
		factura.setCuitSnapshot(sale.getCustomer().getCuit());
		factura.setFullNameSnapshot(sale.getCustomer().getFullName());
		factura.setSale(sale);
		factura.setFileUrl("factura path");
		Factura savedF = repo.save(factura);
		sale.setFactura(savedF);
		return savedF;
	}
	
	public Factura findById(Long id) {
		return repo.findById(id).orElseThrow();
	}
	
	public Factura findBySaleId(Long idSale) {
		return repo.findBySaleId(idSale).orElseThrow();
	}
	
	@Transactional
	public void delete(Long id) {
		repo.deleteById(id);
	}
	@Transactional
	public void deleteBySaleId(Long idSale) {
		repo.deleteBySaleId(idSale);
	}
	
}
