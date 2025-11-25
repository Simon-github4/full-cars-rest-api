package com.fullcars.restapi.facturacion;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fullcars.restapi.dto.ContribuyenteData;
import com.fullcars.restapi.facturacion.enums.CondicionIva;

public class AfipPadronClient {

	//private static final String ENDPOINT_URL = "https://aws.afip.gov.ar/sr-padron/webservices/personaServiceA5";
	private static final String ENDPOINT_URL = "https://aws.afip.gov.ar/sr-padron/webservices/personaServiceA5";

	public static void main(String[] args) {
		try {
			// 1. Datos de prueba (Debes obtenerlos del WSAA previamente)
			String token = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI/Pgo8c3NvIHZlcnNpb249IjIuMCI+CiAgICA8aWQgc3JjPSJDTj13c2FhaG9tbywgTz1BRklQLCBDPUFSLCBTRVJJQUxOVU1CRVI9Q1VJVCAzMzY5MzQ1MDIzOSIgdW5pcXVlX2lkPSIxNTIxODIyOTU2IiBnZW5fdGltZT0iMTc2MzQxMzc3NiIgZXhwX3RpbWU9IjE3NjM0NTcwMzYiLz4KICAgIDxvcGVyYXRpb24gdHlwZT0ibG9naW4iIHZhbHVlPSJncmFudGVkIj4KICAgICAgICA8bG9naW4gZW50aXR5PSIzMzY5MzQ1MDIzOSIgc2VydmljZT0id3Nfc3JfY29uc3RhbmNpYV9pbnNjcmlwY2lvbiIgdWlkPSJTRVJJQUxOVU1CRVI9Q1VJVCAyMDIyODI5MTgyMCwgQ049ZmFjdHVyYWNpb250IiBhdXRobWV0aG9kPSJjbXMiIHJlZ21ldGhvZD0iMjIiPgogICAgICAgICAgICA8cmVsYXRpb25zPgogICAgICAgICAgICAgICAgPHJlbGF0aW9uIGtleT0iMjAyMjgyOTE4MjAiIHJlbHR5cGU9IjQiLz4KICAgICAgICAgICAgPC9yZWxhdGlvbnM+CiAgICAgICAgPC9sb2dpbj4KICAgIDwvb3BlcmF0aW9uPgo8L3Nzbz4K";
			String sign = "FLywIq5HlD5N7okeLpGvRf91+6f6LQskfCJtEqB7g5AiTj5SXpiMNbHatlusdrko+T8CuDJzZmyTtG0HdCEc2LNHYZlpvf5QU/+m4CHmMySRkavG6RTlWKloWnII8MQlLXkJCOFsTZJEiVg+Sgsnm8YfuYUkaNcOUP2ES+Rao+Y=";
			long cuitRepresentada = 20228291820L; // Tu CUIT (el del certificado)
			long idPersona = 33693450239L; // El CUIT del cliente a consultar

			// 2. Llamada al servicio
			ContribuyenteData cliente = getPersonaV2(token, sign, cuitRepresentada, idPersona);

			// 3. Imprimir resultados (Listos para tu PDF)
			System.out.println("--- Datos para el PDF ---");
			System.out.println("Nombre/Razón Social: " + cliente.getNombre());
			System.out.println("Dirección: " + cliente.getDireccion());
			System.out.println("Localidad: " + cliente.getLocalidad());
			System.out.println("Provincia: " + cliente.getProvincia());
			System.out.println("Condición IVA: " + cliente.getCondicionIva());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Consulta los datos de una persona en el Padrón A5 [cite: 1678]
	public static ContribuyenteData getPersonaV2(String token, String sign, long cuitRepresentada, long idPersona)
			throws Exception {

		String soapXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:a5=\"http://a5.soap.ws.server.puc.sr/\">" + "   <soapenv:Header/>" + "   <soapenv:Body>"
				+ "      <a5:getPersona_v2>" + "         <token>" + token + "</token>" + "         <sign>" + sign
				+ "</sign>" + "         <cuitRepresentada>" + cuitRepresentada + "</cuitRepresentada>"
				+ "         <idPersona>" + idPersona + "</idPersona>" + "      </a5:getPersona_v2>"
				+ "   </soapenv:Body>" + "</soapenv:Envelope>";

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ENDPOINT_URL))
				.header("Content-Type", "text/xml; charset=utf-8").header("SOAPAction", "") // Action vacío suele funcionar en AFIP
				.POST(HttpRequest.BodyPublishers.ofString(soapXml)).build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			System.err.println("ERROR: " + response.body()); 
			throw new RuntimeException("Error HTTP: " + response.statusCode() + " - Ver consola para detalle XML.");
		}

		return parsearRespuestaXML(response.body());
	}

	private static ContribuyenteData parsearRespuestaXML(String xmlResponse) throws Exception {
		System.out.println("Respuesta XML completa:\n" + xmlResponse); 
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

	    // 1. Verificar si hay errores de constancia (Bloqueos de AFIP)
	    NodeList erroresConstancia = doc.getElementsByTagName("errorConstancia");
	    if (erroresConstancia != null && erroresConstancia.getLength() > 0) {
	        Element errorNode = (Element) erroresConstancia.item(0);
	        NodeList listaErrores = errorNode.getElementsByTagName("error");
	        
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < listaErrores.getLength(); i++) 
	            sb.append("- ").append(listaErrores.item(i).getTextContent()).append("\n");

	            throw new Exception("El CUIT consultado tiene bloqueos en AFIP:\n" + sb.toString());
	    }
	    
		ContribuyenteData data = new ContribuyenteData();
		// Navegar nodos básicos [cite: 1715-1725]
		Element datosGenerales = (Element) doc.getElementsByTagName("datosGenerales").item(0);

		if (datosGenerales != null) {
			// Nombre: Prioriza Razón Social, si no existe concatena Apellido y Nombre
			String razonSocial = getTagValue("razonSocial", datosGenerales);
			String apellido = getTagValue("apellido", datosGenerales);
			String nombre = getTagValue("nombre", datosGenerales);

			if (razonSocial != null && !razonSocial.isEmpty())
				data.setNombre(razonSocial);
			else
				data.setNombre((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : ""));

			// Domicilio Fiscal
			Element domicilio = (Element) datosGenerales.getElementsByTagName("domicilioFiscal").item(0);
			if (domicilio != null) {
				data.setDireccion(getTagValue("direccion", domicilio));
				data.setLocalidad(getTagValue("localidad", domicilio));
				data.setProvincia(getTagValue("descripcionProvincia", domicilio));
				data.setCodigoPostal(getTagValue("codPostal", domicilio));
			}
		}

		// Determinar Condición frente al IVA, Buscamos en la lista de impuestos [cite: 1728, 2016, 2069]
		NodeList impuestos = doc.getElementsByTagName("impuesto"); // Busca tanto en RegimenGeneral como Monotributo
		data.setCondicionIva(CondicionIva.CONSUMIDOR_FINAL); //Valor por defecto si no encuentra impuestos relevantes (ej: solo tiene Bienes Personales)

		for (int i = 0; i < impuestos.getLength(); i++) {
			Element imp = (Element) impuestos.item(i);
			String idImpuesto = getTagValue("idImpuesto", imp);

			if ("30".equals(idImpuesto) || "20".equals(idImpuesto) || "32".equals(idImpuesto)) { 
				data.setCondicionIva(CondicionIva.fromCodigo(idImpuesto));
				break;
			}
		}

		return data;
	}

	private static String getTagValue(String tag, Element element) {
		NodeList nodeList = element.getElementsByTagName(tag);
		if (nodeList != null && nodeList.getLength() > 0)
			return nodeList.item(0).getTextContent();
		return "";
	}

}