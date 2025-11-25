package com.fullcars.restapi.facturacion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.dto.CAEResponse;
import com.fullcars.restapi.dto.DatosFacturacion;
import com.fullcars.restapi.facturacion.enums.Factura;
import com.fullcars.restapi.facturacion.enums.IvaAlicuota;
import com.fullcars.restapi.facturacion.enums.TipoDocumento;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;
import com.fullcars.restapi.model.Sale;

/**
 * Servicio para generar CAE utilizando el Web Service WSFEV1 de AFIP. Este
 * servicio (a diferencia de WSMTXCA) requiere SOAPAction y no maneja detalle de
 * ítems en la solicitud, solo totales.
 */
public class WSFEV1Service extends WSFEV1Client implements Factura {

	// Proceso principal para generar un CAE.
	@Override
	public CAEResponse generarCAE(AfipAuth auth, Sale sale, DatosFacturacion datos, long ultimoComp) throws Exception {
		long proximoNumero = ultimoComp + 1;
		System.out.println("Próximo número a autorizar: " + proximoNumero);

		String soapRequest = buildFECAESolicitarRequest(auth, sale, datos, proximoNumero);
		System.out.println("Enviando solicitud de autorización (WSFEV1)..." + soapRequest); // Descomentar para debug
																							// pesado

		String soapResponse = invokeWS(soapRequest, "FECAESolicitar");
		System.out.println("Respuesta recibida." + soapResponse); // Descomentar para debug pesado

		return parseFECAESolicitarResponse(soapResponse, proximoNumero);
	}

	// Construye el XML principal para 'FECAESolicitar' (WSFEV1)
	private String buildFECAESolicitarRequest(AfipAuth auth, Sale sale, DatosFacturacion datos,
			long numeroComprobante) {

		// --- Cálculos de Totales ---
		IvaAlicuota alicuota = datos.getAlicuota();
		BigDecimal netoTotal = sale.getTotal(); // Asumimos que sale.getTotal() es el NETO
		BigDecimal ivaTotal = netoTotal.multiply(alicuota.getMultiplicador()).setScale(2, RoundingMode.HALF_UP);
		BigDecimal totalComprobante = netoTotal.add(ivaTotal).setScale(2, RoundingMode.HALF_UP);
		String fecha = sale.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		long codigoCondicionIvaReceptor;
		TiposComprobante tipoComprobante = datos.getTipoComprobante();
		// Ajusta estos códigos si usás otros (ej. Nota de Crédito A/B)
		if (tipoComprobante == TiposComprobante.FACTURA_A || tipoComprobante == TiposComprobante.FACTURA_B) {
			codigoCondicionIvaReceptor = (tipoComprobante == TiposComprobante.FACTURA_A) ? 1L : 5L; // 1=Resp.
																									// Inscripto,
																									// 5=Cons. Final
		} else {
			// Por defecto, asumimos Consumidor Final para otros comprobantes B (ej. Nota
			// Credito B)
			// O Resp. Inscripto para otros comprobantes A. ¡Esta lógica puede necesitar
			// ajuste según tu caso!
			codigoCondicionIvaReceptor = (datos.getTipoDocumento() == TipoDocumento.CUIT) ? 1L : 5L;
		}

		// --- Bloque <Iva> (Desglose de alícuotas) ---
		// WSFEV1 no usa 'arrayIva', sino un tag 'Iva' que contiene 'AlicIva'
		String ivaXml = String.format(
				"<ar:Iva>" + "  <ar:AlicIva>" + "    <ar:Id>%d</ar:Id>" + "    <ar:BaseImp>%s</ar:BaseImp>"
						+ "    <ar:Importe>%s</ar:Importe>" + "  </ar:AlicIva>" + "</ar:Iva>",
				alicuota.getCodigo(), netoTotal.toString(), ivaTotal.toString());

		// --- Plantilla SOAP ---
		String soapTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ar=\"http://ar.gov.afip.dif.FEV1/\">"
				+ "<soapenv:Header/>" + "<soapenv:Body>" + "  <ar:FECAESolicitar>" + "    <ar:Auth>"
				+ "       <ar:Token>%s</ar:Token>" + "       <ar:Sign>%s</ar:Sign>" + "       <ar:Cuit>%d</ar:Cuit>"
				+ "    </ar:Auth>" + "    <ar:FeCAEReq>" + "       <ar:FeCabReq>"
				+ "           <ar:CantReg>1</ar:CantReg>" + // Siempre 1 comprobante a la vez
				"           <ar:PtoVta>%d</ar:PtoVta>" + "           <ar:CbteTipo>%d</ar:CbteTipo>"
				+ "       </ar:FeCabReq>" + "       <ar:FeDetReq>" + "           <ar:FECAEDetRequest>"
				+ "               <ar:Concepto>%d</ar:Concepto>" + "               <ar:DocTipo>%d</ar:DocTipo>"
				+ "               <ar:DocNro>%d</ar:DocNro>"
				+ "               <ar:CondicionIVAReceptorId>%d</ar:CondicionIVAReceptorId>"
				+ "               <ar:CbteDesde>%d</ar:CbteDesde>" + "               <ar:CbteHasta>%d</ar:CbteHasta>"
				+ "               <ar:CbteFch>%s</ar:CbteFch>" + "               <ar:ImpTotal>%s</ar:ImpTotal>"
				+ "               <ar:ImpTotConc>0</ar:ImpTotConc>" + // Importe No Gravado
				"               <ar:ImpNeto>%s</ar:ImpNeto>" + "               <ar:ImpOpEx>0</ar:ImpOpEx>" + // Importe
																												// Exento
				"               <ar:ImpTrib>0</ar:ImpTrib>" + // Importe Tributos
				"               <ar:ImpIVA>%s</ar:ImpIVA>" + "               <ar:MonId>PES</ar:MonId>"
				+ "               <ar:MonCotiz>1</ar:MonCotiz>" + "               %s" + // Aquí va ivaXml
				"           </ar:FECAEDetRequest>" + "       </ar:FeDetReq>" + "    </ar:FeCAEReq>"
				+ "  </ar:FECAESolicitar>" + "</soapenv:Body>" + "</soapenv:Envelope>";

		// Rellenar la plantilla
		return String.format(soapTemplate, auth.getToken(), auth.getSign(), datos.getCuitEmisor(),
				datos.getPuntoVenta(), datos.getTipoComprobante().getCodigo(), datos.getConcepto().codigo(),
				datos.getTipoDocumento().getCodigo(), datos.getNumeroDocumento(), codigoCondicionIvaReceptor,
				numeroComprobante, // CbteDesde
				numeroComprobante, // CbteHasta
				fecha, totalComprobante.toString(), netoTotal.toString(), ivaTotal.toString(), ivaXml // El bloque de
																										// desglose de
																										// IVA
		);
	}

	// Parsea la respuesta de 'FECAESolicitar'
	private CAEResponse parseFECAESolicitarResponse(String soapResponse, long numeroComprobanteAsignado)
			throws Exception {
		CAEResponse response = new CAEResponse();
		Document doc = parseXml(soapResponse);

		// Buscar errores
		NodeList errors = doc.getElementsByTagName("Err");
		if (errors.getLength() > 0) {
			String msg = getTagValue(errors.item(0), "Msg");
			response.setError(msg != null ? msg : "Error desconocido de AFIP.");
			return response;
		}

		// Buscar resultado exitoso
		NodeList detResponse = doc.getElementsByTagName("FECAEDetResponse");
		if (detResponse.getLength() > 0) {
			Node detNode = detResponse.item(0);

			String resultado = getTagValue(detNode, "Resultado");

			if ("A".equals(resultado)) { // A = Aprobado
				response.setCae(getTagValue(detNode, "CAE"));
				response.setFechaVencimiento(getTagValue(detNode, "CAEFchVto"));
				response.setNumeroComprobante(numeroComprobanteAsignado);

				// Buscar Observaciones (si las hay)
				NodeList obs = doc.getElementsByTagName("Observaciones");
				if (obs.getLength() > 0 && obs.item(0).hasChildNodes()) {
					response.setObservaciones(getTagValue(obs.item(0).getFirstChild(), "Msg"));
				}
				return response;

			} else { // R = Rechazado
				NodeList obs = doc.getElementsByTagName("Observaciones");
				if (obs.getLength() > 0 && obs.item(0).hasChildNodes()) {
					response.setError("Rechazado: " + getTagValue(obs.item(0).getFirstChild(), "Msg"));
				} else {
					response.setError("Rechazado por AFIP (sin observaciones).");
				}
				return response;
			}
		}

		response.setError("Respuesta SOAP no reconocida.");
		return response;
	}

}