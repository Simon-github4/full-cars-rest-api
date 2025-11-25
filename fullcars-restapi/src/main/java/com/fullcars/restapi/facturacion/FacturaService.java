package com.fullcars.restapi.facturacion;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.IFacturaRepository;
import com.fullcars.restapi.service.SaleService;

@Service
public class FacturaService {

	private final IFacturaRepository repo;
	private final SaleService saleService;

	public FacturaService(IFacturaRepository repo, SaleService saleService) {
		this.repo = repo;
		this.saleService = saleService;
	}
	
	/*
	 public FacturaResponse emitirFactura(Long saleId, TiposComprobante tipoC) {
	 
        Sale sale = saleService.findByIdOrThrow(saleId);

        // Selección del cliente según tipo de comprobante
        FacturaClient client = facturaClientFactory.getClient();

        FacturaResponse response = client.solicitarCAE(sale);

        crear FACT
        sale.setCae(response.getCae());
        sale.setCaeVencimiento(response.getVencimiento());
        saleRepository.save(sale);
       sale.setFactura();

       return response;
    }
*/
	
	@Transactional
	@EventListener
	public void handleSaleEvent(SaleEvent e) {
		System.err.println("Facuta Service Received SaleEvent !!!" + e.getSource());
		Sale sale = e.getEntity();
		if(e.getEventType().equals(EventType.DELETE))
			deleteBySaleId(sale.getId());
		else if(e.getEventType().equals(EventType.INSERT) && sale.getSaleNumber() != null && !sale.getSaleNumber().isBlank())
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
