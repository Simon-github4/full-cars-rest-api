package com.fullcars.restapi.facturacion;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.dto.CAEResponse;
import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.facturacion.enums.Servicios;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;
import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.IFacturaRepository;
import com.fullcars.restapi.service.SaleService;

@Service
public class FacturaService {

	private final IFacturaRepository repo;
	private final SaleService saleService;
	private final ArcaTokenCacheService tokenService;
	private final AfipConfig afipConfig;
	
	public FacturaService(IFacturaRepository repo, SaleService saleService, ArcaTokenCacheService tokenService, AfipConfig afipConfig) {
		this.repo = repo;
		this.saleService = saleService;
		this.tokenService = tokenService;
		this.afipConfig = afipConfig;
	}
	
	 public CAEResponse emitirFactura(Long saleId, TiposComprobante tipoC) {
	 
        Sale sale = saleService.findByIdOrThrow(saleId);

        // Selección del cliente según tipo de comprobante
        /*FacturaClient client = facturaClientFactory.getClient();

        CAEResponse response = client.solicitarCAE(sale);

        sale.setCae(response.getCae());
        sale.setCaeVencimiento(response.getVencimiento());
        saleRepository.save(sale);
        sale.setFactura();
         */
       return null;//response
    }
	
	@Transactional
	@EventListener
	public void handleSaleEvent(SaleEvent e) {
		System.err.println("Facuta Service Received SaleEvent !!!" + e.getSource());
		Sale sale = e.getEntity();
		if(e.getEventType().equals(EventType.DELETE))
			deleteBySaleId(sale.getId());
		//else if(e.getEventType().equals(EventType.INSERT) && sale.getSaleNumber() != null && !sale.getSaleNumber().isBlank())
			//save(sale);
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

	public String getAfipData() {
		StringBuilder sb = new StringBuilder();
		//sb.append("AFIP DATA\n").append(afipConfig.getCuit()).append(afipConfig.getWsfev1Endpoint())
		return tokenService.getTicket(Servicios.CONSTANCIA_INSCRIPCION).toString();
	}
	
}
