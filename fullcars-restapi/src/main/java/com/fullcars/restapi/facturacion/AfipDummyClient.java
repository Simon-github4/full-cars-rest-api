package com.fullcars.restapi.facturacion;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class AfipDummyClient {

	// Para Testing usar: "https://wswhomo.afip.gov.ar/wsfev1/service.asmx"
	private static final String ENDPOINT_PROD = "https://servicios1.afip.gov.ar/wsfev1/service.asmx";

	public static void main(String[] args) {
		try {
			checkServerStatus();
		} catch (Exception e) {
			System.err.println("Error al consultar: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void checkServerStatus() throws Exception {
		System.out.println("Consultando estado de servidores AFIP (FEDummy)...");
		// 1. Construcción del XML Request (Sin Auth según manual Pag 118)
		String soapXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:ar=\"http://ar.gov.afip.dif.FEV1/\">" + "   <soapenv:Header/>" + "   <soapenv:Body>"
				+ "      <ar:FEDummy/>" + "   </soapenv:Body>" + "</soapenv:Envelope>";

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ENDPOINT_PROD))
				.header("Content-Type", "text/xml; charset=utf-8")
				.header("SOAPAction", "http://ar.gov.afip.dif.FEV1/FEDummy")
				.POST(HttpRequest.BodyPublishers.ofString(soapXml)).build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200)
			throw new RuntimeException("Error HTTP: " + response.statusCode() + "\nBody: " + response.body());

		parsearYMostrarRespuesta(response.body());
	}

	private static void parsearYMostrarRespuesta(String xmlResponse) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

		System.out.println("--- Estado de Infraestructura AFIP ---");
		System.out.println("AppServer:  " + getTagValue("AppServer", doc));
		System.out.println("DbServer:   " + getTagValue("DbServer", doc));
		System.out.println("AuthServer: " + getTagValue("AuthServer", doc));
		System.out.println("--------------------------------------");
	}

	private static String getTagValue(String tag, Document doc) {
		NodeList nodeList = doc.getElementsByTagName(tag);
		if (nodeList != null && nodeList.getLength() > 0) {
			return nodeList.item(0).getTextContent();
		}
		return "N/A";
	}
}