package com.fullcars.restapi.facturacion;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse; 
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class WSFEV1Client {

	//protected static final String WSFEV1_ENDPOINT = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx";
	//protected static final String WSFEV1_ENDPOINT = "https://servicios1.afip.gov.ar/wsfev1/service.asmx";
	
	//protected static final String WSFEV1_SERVICE_URL = "http://ar.gov.afip.dif.FEV1/";

	// Método helper para enviar la solicitud SOAP (CON SOAPAction)
	protected static String invokeWS(String soapRequest, String methodName, String endpoint, String service) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(endpoint); // Apunta al endpoint de WSFEV1

		String soapAction = service + methodName;

		httpPost.setHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.setHeader("SOAPAction", soapAction);
		httpPost.setEntity(new StringEntity(soapRequest, "UTF-8"));
		// System.out.println("Enviando soapRequest: "+soapRequest);

		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity, StandardCharsets.UTF_8);

		if (response.getStatusLine().getStatusCode() != 200) {
			System.err.println("Error HTTP: " + response.getStatusLine().getStatusCode());
			System.err.println("Respuesta: " + responseText);
			throw new Exception("Error HTTP: " + response.getStatusLine().getStatusCode());
		}

		return responseText;
	}

	// Helper para parsear XML
	protected static Document parseXml(String xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// WSFEV1 no usa namespaces en la respuesta, así que lo simplificamos
		dbf.setNamespaceAware(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	// Helper para obtener el valor de un tag (simplificado, sin namespaces)
	protected static String getTagValue(Node node, String tagName) {
		if (node == null)
			return null;

		// Si el nodo actual es el que buscamos
		if (node.getNodeType() == Node.ELEMENT_NODE && tagName.equals(node.getNodeName())) {
			return node.getTextContent();
		}

		// Buscar en los hijos
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (tagName.equals(child.getNodeName())) {
					return child.getTextContent();
				}
			}
		}
		return null;
	}

	protected static String buildEnvelope(String bodyContent) {
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:ar=\"http://ar.gov.afip.dif.FEV1/\">" + "<soapenv:Header/>" + "<soapenv:Body>" + bodyContent
				+ "</soapenv:Body>" + "</soapenv:Envelope>";
	}
}
