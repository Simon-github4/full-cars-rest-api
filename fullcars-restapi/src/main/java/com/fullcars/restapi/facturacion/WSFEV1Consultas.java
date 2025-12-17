package com.fullcars.restapi.facturacion;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.dto.DatosFacturacion;

public class WSFEV1Consultas extends WSFEV1Client {

	// Llama al método 'FECompUltimoAutorizado'
	public static long consultarUltimoComprobanteFEV1(AfipAuth auth, DatosFacturacion datos, String endpoint, String service) throws Exception {
		String soapRequest = buildConsultaUltimoRequestFEV1(auth, datos);
		String soapResponse = invokeWS(soapRequest, "FECompUltimoAutorizado", endpoint, service);

		Document doc = parseXml(soapResponse);

		// Revisar errores
		NodeList errors = doc.getElementsByTagName("Err");
		if (errors.getLength() > 0) {
			String msg = getTagValue(errors.item(0), "Msg");
			throw new Exception("Error al consultar último número: " + msg);
		}

		NodeList nodes = doc.getElementsByTagName("CbteNro");
		if (nodes.getLength() > 0)
			return Long.parseLong(nodes.item(0).getTextContent());

		return 0;
	}

	// Construye el XML para 'FECompUltimoAutorizado' (WSFEV1)
	private static String buildConsultaUltimoRequestFEV1(AfipAuth auth, DatosFacturacion datos) {
		String template = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "                  xmlns:ar=\"http://ar.gov.afip.dif.FEV1/\">" + "<soapenv:Header/>"
				+ "<soapenv:Body>" + "  <ar:FECompUltimoAutorizado>" + "    <ar:Auth>"
				+ "       <ar:Token>%s</ar:Token>" + "       <ar:Sign>%s</ar:Sign>" + "       <ar:Cuit>%d</ar:Cuit>"
				+ "    </ar:Auth>" + "    <ar:PtoVta>%d</ar:PtoVta>" + "    <ar:CbteTipo>%d</ar:CbteTipo>"
				+ "  </ar:FECompUltimoAutorizado>" + "</soapenv:Body>" + "</soapenv:Envelope>";

		return String.format(template, auth.getToken(), auth.getSign(), datos.getCuitEmisor(), datos.getPuntoVenta(),
				datos.getTipoComprobante().getCodigo());
	}

}
