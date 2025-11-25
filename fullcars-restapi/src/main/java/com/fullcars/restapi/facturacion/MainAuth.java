package com.fullcars.restapi.facturacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.dto.DatosFacturacion;
import com.fullcars.restapi.facturacion.enums.Conceptos;
import com.fullcars.restapi.facturacion.enums.IvaAlicuota;
import com.fullcars.restapi.facturacion.enums.TipoDocumento;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;
import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.model.SaleDetail;


public class MainAuth {
	public static void main(String[] args) {
		try {
			//AfipDummyClient.checkServerStatus();
			//AfipAuth auth = WSAAClient.authenticate(Servicios.CONSTANCIA_INSCRIPCION);
			
			System.out.println( AfipPadronClient.getPersonaV2(auth.getToken(), auth.getSign(), CUIT_EMISOR, 23461064639L) );
			//generarCAE();

			// System.out.println("Token: " + auth.getToken());
			// System.out.println("Sign: " + auth.getSign());
			// System.out.println("Expira: " + auth.getExpirationTime());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static final long CUIT_EMISOR = 30718338502L; 
	//private static final long CUIT_EMISOR = 20228291820L;
	private static final AfipAuth auth = new AfipAuth(
			"PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI/Pgo8c3NvIHZlcnNpb249IjIuMCI+CiAgICA8aWQgc3JjPSJDTj13c2FhLCBPPUFGSVAsIEM9QVIsIFNFUklBTE5VTUJFUj1DVUlUIDMzNjkzNDUwMjM5IiB1bmlxdWVfaWQ9IjQyNzA2NjAzNDQiIGdlbl90aW1lPSIxNzYzNTg2NzM1IiBleHBfdGltZT0iMTc2MzYyOTk5NSIvPgogICAgPG9wZXJhdGlvbiB0eXBlPSJsb2dpbiIgdmFsdWU9ImdyYW50ZWQiPgogICAgICAgIDxsb2dpbiBlbnRpdHk9IjMzNjkzNDUwMjM5IiBzZXJ2aWNlPSJ3c19zcl9jb25zdGFuY2lhX2luc2NyaXBjaW9uIiB1aWQ9IlNFUklBTE5VTUJFUj1DVUlUIDMwNzE4MzM4NTAyLCBDTj1zb2Z0d2FyZSBmdWxsY2FycyIgYXV0aG1ldGhvZD0iY21zIiByZWdtZXRob2Q9IjIyIj4KICAgICAgICAgICAgPHJlbGF0aW9ucz4KICAgICAgICAgICAgICAgIDxyZWxhdGlvbiBrZXk9IjMwNzE4MzM4NTAyIiByZWx0eXBlPSI0Ii8+CiAgICAgICAgICAgIDwvcmVsYXRpb25zPgogICAgICAgIDwvbG9naW4+CiAgICA8L29wZXJhdGlvbj4KPC9zc28+Cg==",
			"EAP+42oKHlkmiQZl2x0mTt0iFebfflHskUvSMlO5tyb8HGnk+6RinfZN2GFssWXYxQtpXNQ7INBQwLX4jyLT4V4Bw1/2X8Cj8/a8WRy9Cyq0pIRB4feeQPgBz471dGMlnkeiiRb4jIAa3Dmt9sFxqX7yts3Lg2wjbYFnYDBS8Og=",
			java.time.LocalDateTime.now().plusHours(12)
	);
	
	public static void generarCAE() {
		Sale sale = getTestSale();

		DatosFacturacion datos = new DatosFacturacion(
				CUIT_EMISOR, 
				TiposComprobante.FACTURA_B,		// Código Comprobante (6 = Factura B)
				Conceptos.PRODUCTOS, 			// Código Concepto (1 = Productos)
				TipoDocumento.CONSUMIDOR_FINAL, // Código Doc (96 = Consumidor Final)
				0, 								// Número Doc (0 para Cons. Final)
				IvaAlicuota.IVA_21 				// Alícuota de IVA para toda la factura
		);

		WSFEV1Service servicioAFIP = new WSFEV1Service();

		try {
			long ultimoComp = new WSFEV1Consultas().consultarUltimoComprobanteFEV1(auth, datos);
			System.out.println("Resultado: " + ultimoComp);

			//CAEResponse respuesta = servicioAFIP.generarCAE(auth, sale, datos, ultimoComp);

			/*
			 * if (respuesta.getError() == null) {
			 * System.out.println("¡CAE generado con éxito!"); // Aquí guardas
			 * respuesta.getCae() y respuesta.getNumeroComprobante() en tu BBDD }
			 */

		} catch (Exception e) {
			System.err.println("Falló la generación de CAE:");
			e.printStackTrace();
		}
	}

	private static Sale getTestSale() {
		Sale sale = new Sale(); // Asumimos que llenas esto con 'date' y 'details'
		sale.setId(1L);
		sale.setDate(LocalDate.now());
		List<SaleDetail> details = new ArrayList<>();
		CarPart repuesto1 = new CarPart();
		repuesto1.setId(101L);
		repuesto1.setName("Filtro de Aceite");
		repuesto1.setDescription("Filtro de aceite premium para motores 1.6L");
		repuesto1.setSku("SKU-FIL-001");
		repuesto1.setStock(50L);
		repuesto1.setBasePrice(new BigDecimal("2.50"));
		SaleDetail sd = new SaleDetail();
		sd.setSale(sale);
		sd.setCarPart(repuesto1);
		sd.setUnitPrice(new BigDecimal("2.50"));
		sd.setQuantity(1);
		details.add(sd);
		sale.setDetails(details);
		
		return sale;
	}
}
