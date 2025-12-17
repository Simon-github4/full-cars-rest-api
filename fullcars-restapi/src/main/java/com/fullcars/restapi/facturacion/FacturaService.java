package com.fullcars.restapi.facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.ServerException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.dto.CAEResponse;
import com.fullcars.restapi.dto.ContribuyenteData;
import com.fullcars.restapi.dto.DatosFacturacion;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.facturacion.enums.Conceptos;
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
	private static final String RUTA_BASE_PDFS = "C:/SoftwareFullCars/FacturasEmitidas/";
	
	public FacturaService(IFacturaRepository repo, SaleService saleService, ArcaTokenCacheService tokenService, AfipConfig afipConfig) {
		this.repo = repo;
		this.saleService = saleService;
		this.tokenService = tokenService;
		this.afipConfig = afipConfig;
	}
	
	public CAEResponse emitirCae(Sale sale, DatosFacturacion datosFact) throws Exception {
		AfipAuth auth = tokenService.getTicket(Servicios.FACTURACION_ELECTRONICA);
        long ultimoComp = WSFEV1Consultas.consultarUltimoComprobanteFEV1(
        		auth,
        		datosFact, 
        		afipConfig.getWsfev1Endpoint(),
        		afipConfig.getWsfev1ServiceUrl());
        
       return WSFEV1Service.generarCAE(
        		auth,
        		sale,
        		datosFact,
        		ultimoComp,
        		afipConfig.getWsfev1Endpoint(),
        		afipConfig.getWsfev1ServiceUrl());
    }
	
	public byte[] generarFactura(Long saleId, TiposComprobante tiposComprobante, Long idReceptor) throws Exception {
		Sale sale = saleService.findByIdOrThrow(saleId);
		
		idReceptor = Long.parseLong(sale.getCustomer().getCuit());
		
		DatosFacturacion datosFact = new DatosFacturacion(afipConfig.getCuit(), tiposComprobante, idReceptor);
		ContribuyenteData receptor = consultarContribuyente(tokenService.getTicket(Servicios.CONSTANCIA_INSCRIPCION), idReceptor);
		CAEResponse response = emitirCae(sale, datosFact);

        Factura fact = mapearFactura(sale, datosFact, receptor, response);
		
        if (response != null && response.getCae() != null) {	
            try {
            	Factura saved = save(fact);
	        	byte[] pdfBytes = FacturaPDFGenerator.generarFacturaPDF(fact, datosFact.getAlicuota());
	
	            String pathGuardado = guardarPdfEnDisco(pdfBytes, generarNombreArchivo(fact));
	            saved.setFileUrl(pathGuardado);
	            save(saved);
	            
				return pdfBytes;
				
            } catch (Exception e) {
				e.printStackTrace();
				throw new Exception("CAE generado. Error al generar o guardar el PDF");
			}
        }else
			throw new RuntimeException("No se pudo generar la factura electrónica.");
	}
	
	public ContribuyenteData consultarContribuyente(AfipAuth auth, long idBuscado) throws ServerException {
		try {
			return AfipPadronClient.getPersonaV2(auth.getToken(), auth.getSign(), afipConfig.getCuit(), idBuscado, afipConfig.getPadronEndpoint());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException("Error al consultar el padrón de AFIP");
		}
	}
	
	@Transactional
	@EventListener
	public void handleSaleEvent(SaleEvent e) {
		System.err.println("Facuta Service Received SaleEvent !!!" + e.getSource());
		Sale sale = e.getEntity();
		//if(e.getEventType().equals(EventType.DELETE))
		//	deleteBySaleId(sale.getId());
		//else if(e.getEventType().equals(EventType.INSERT) && sale.getSaleNumber() != null && !sale.getSaleNumber().isBlank())
			//save(sale);
	}
	
	@Transactional
	public Factura save(Factura factura) {
		return repo.save(factura);
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
		return tokenService.getTicket(Servicios.CONSTANCIA_INSCRIPCION).toString().concat(afipConfig.getWsfev1Endpoint());
	}

	public static Factura mapearFactura(Sale sale, DatosFacturacion datos, ContribuyenteData contribuyente,
			CAEResponse caeResponse) {

		Factura factura = new Factura();

		factura.setSale(sale); 

		factura.setCuitEmisor(datos.getCuitEmisor());
		factura.setPuntoVenta(datos.getPuntoVenta());
		factura.setTipoComprobante(datos.getTipoComprobante().getCodigo());
		//caeresponse mas abajo(numCompr tmb)
		factura.setFechaEmision(LocalDate.now());
		factura.setConcepto(datos.getConcepto()); 
		factura.setCuitCliente(datos.getNumeroDocumento()); // El DNI/CUIT viene de DatosFacturacion
		factura.setTipoDocCliente(datos.getTipoDocumento().getCodigo());

		// Datos visuales del cliente (Desde ContribuyenteData)
		factura.setRazonSocialCliente(contribuyente.getNombre());
		factura.setDomicilioCliente(contribuyente.getDomicilioComercialFormateado()); // Usando el helper que creamos
		factura.setCondicionIvaCliente(contribuyente.getCondicionIva());

		// 4. Cálculos de Importes (Basado en Sale y la alícuota de DatosFacturacion)
		BigDecimal neto = sale.getTotal();
		BigDecimal multiplicadorIva = datos.getAlicuota().getMultiplicador(); // Ej: 0.21

		BigDecimal importeIva = neto.multiply(multiplicadorIva).setScale(2, RoundingMode.HALF_UP);
		BigDecimal totalVenta = neto.add(importeIva).setScale(2, RoundingMode.HALF_UP);

		factura.setImpNeto(neto);
		factura.setImpIva(importeIva);
		factura.setImpTotal(totalVenta);

		/* Seteo de valores en 0 por defecto (ya que DatosFacturacion no los provee dinámicamente)
		factura.setImpNoGravado(BigDecimal.ZERO);
		factura.setImpExento(BigDecimal.ZERO);
		factura.setImpTributos(BigDecimal.ZERO);*/

		// 5. Respuesta de AFIP (Desde CAEResponse)
		if (caeResponse != null) {
			factura.setNumeroComprobante(caeResponse.getNumeroComprobante());
			factura.setCae(caeResponse.getCae());

			// Parseo de fecha String (yyyyMMdd) a LocalDate
			if (caeResponse.getFechaVencimiento() != null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				factura.setVtoCae(LocalDate.parse(caeResponse.getFechaVencimiento(), formatter));
			}

			factura.setObservaciones(caeResponse.getObservaciones());

			// Determinar resultado (Si hay CAE es Aprobado, sino Rechazado)
			String resultado = (caeResponse.getCae() != null && !caeResponse.getCae().isEmpty()) ? "A" : "R";
			factura.setResultadoAfip(resultado);
		}

		// 6. Fechas adicionales
		// Si el concepto es Servicios (2) o Productos y Servicios (3), se requiere vto pago.
		// Por defecto ponemos hoy o calculamos 10 días.
		if (datos.getConcepto() != Conceptos.PRODUCTOS) {
			factura.setFechaVencimientoPago(LocalDate.now().plusDays(30)); // Lógica de negocio
		} else {
			factura.setFechaVencimientoPago(null);
		}

		return factura;
	}
	
	// --- Métodos Auxiliares ---
	private String guardarPdfEnDisco(byte[] contenido, String nombreArchivo) throws Exception {
	    try {
	        // Organizar por año es buena práctica para no saturar una carpeta
	        String anio = String.valueOf(java.time.LocalDate.now().getYear());
	        java.nio.file.Path directorioDestino = java.nio.file.Paths.get(RUTA_BASE_PDFS, anio);
	        
	        if (!java.nio.file.Files.exists(directorioDestino)) 
	            java.nio.file.Files.createDirectories(directorioDestino);

	        java.nio.file.Path archivoDestino = directorioDestino.resolve(nombreArchivo);
	        
	        // Escribir el archivo
	        java.nio.file.Files.write(archivoDestino, contenido);
	        
	        return archivoDestino.toAbsolutePath().toString();
	    } catch (java.io.IOException e) {
	        e.printStackTrace();
	        throw new Exception("Error al intentar guardar el PDF en disco: " + e.getMessage());
	    }
	}

	private String generarNombreArchivo(Factura fact) {
	    // Formato: TIPO-PTOVTA-NUMERO.pdf (Ej: FA-00003-00000123.pdf)
	    return String.format("F_%c_%05d-%08d.pdf", 
	            TiposComprobante.fromCodigo(fact.getTipoComprobante()).getTipo(), 
	            fact.getPuntoVenta(), 
	            fact.getNumeroComprobante());
	}
	
}
