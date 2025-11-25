package com.fullcars.restapi.facturacion;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.http.HttpEntity;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.facturacion.enums.Servicios;

public class WSAAClient {

	private static final String RESPONSES_PATH = "C:\\SoftwareFullCars\\XMLresponses\\";

	public static AfipAuth authenticate(Servicios servicio) throws Exception {
		Properties config = new Properties();
		config.load(WSAAClient.class.getClassLoader().getResourceAsStream("wsaa_client.properties"));

		String service = servicio.nombre();
		String endpoint = config.getProperty("endpoint");
		String crtFile = config.getProperty("crtFile");
		String keyFile = config.getProperty("keyFile");
		Long ticketTime = Long.parseLong(config.getProperty("TicketTime", "3600000"));

		String dstDN = config.getProperty("dstdn");// omitido en xml
		// String keyPassword = config.getProperty("keyPassword");

		System.out.println("Endpoint: " + endpoint + "\nService: " + service);

		byte[] cmsData = createCMSFromSeparateFiles(crtFile, keyFile, dstDN, service, ticketTime);
		System.out.println("CMS generado: " + cmsData.length + " bytes");

		String response = invokeWSAA(cmsData, endpoint);
		System.out.println("Respuesta recibida, longitud: " + response.length() + " caracteres");

		Files.write(Paths.get(RESPONSES_PATH + service + UUID.randomUUID()), response.getBytes(StandardCharsets.UTF_8));
		System.out.println("Respuesta guardada");

		return processResponse(response);
	}

	private static byte[] createCMSFromSeparateFiles(String crtFile, String keyFile, String dstDN, String service,
			Long ticketTime) throws Exception {

		Security.addProvider(new BouncyCastleProvider());

		System.out.println("Leyendo certificado: " + crtFile);
		X509Certificate certificate = readCertificateFromFile(crtFile);
		System.out.println("Certificado leído: " + certificate.getSubjectDN());

		System.out.println("Leyendo clave privada: " + keyFile);
		PrivateKey privateKey = readPrivateKeyFromFile(keyFile);
		System.out.println("Clave privada leída");

		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
		Security.addProvider(new BouncyCastleProvider());

		ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);

		JcaCertStore certs = new JcaCertStore(Collections.singletonList(certificate));
		generator.addCertificates(certs);

		SignerInfoGenerator signerInfo = new JcaSignerInfoGeneratorBuilder(
				new JcaDigestCalculatorProviderBuilder().build()).build(signer, certificate);

		generator.addSignerInfoGenerator(signerInfo);

		String xmlContent = createLoginTicketRequest(certificate.getSubjectDN().toString(), dstDN, service, ticketTime);
		System.err.print("XML request generado: ");
		System.out.println(xmlContent);

		CMSTypedData msg = new CMSProcessableByteArray(xmlContent.getBytes(StandardCharsets.UTF_8));
		return generator.generate(msg, true).getEncoded();
	}

	private static X509Certificate readCertificateFromFile(String crtFile) throws Exception {
		try (FileInputStream fis = new FileInputStream(crtFile)) {
			java.security.cert.CertificateFactory certFactory = java.security.cert.CertificateFactory
					.getInstance("X.509");
			return (X509Certificate) certFactory.generateCertificate(fis);
		}
	}

	private static PrivateKey readPrivateKeyFromFile(String keyFile) throws Exception {
		String keyContent = new String(Files.readAllBytes(new File(keyFile).toPath()), "UTF-8");
		if (keyContent.contains("BEGIN PRIVATE KEY")) {
			// System.out.println("✅ Formato PKCS#8 sin encriptar");
			return readPkcs8PrivateKey(keyContent);
		} else {
			throw new Exception("Formato de clave no reconocido. Debe ser PKCS#8 sin password");
		}
	}

	private static PrivateKey readPkcs8PrivateKey(String keyContent) throws Exception {
		keyContent = keyContent.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		System.out.println("Longitud base64: " + keyContent.length());
		byte[] keyBytes = Base64.getDecoder().decode(keyContent);
		System.out.println("Bytes decodificados: " + keyBytes.length);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
		return keyFactory.generatePrivate(keySpec);
	}

	private static String createLoginTicketRequest(String signerDN, String dstDN, String service, Long ticketTime) {
		long currentTime = System.currentTimeMillis();
		long expirationTime = currentTime + ticketTime;
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		String genTime = sdf.format(new java.util.Date(currentTime));
		String expTime = sdf.format(new java.util.Date(expirationTime));
		// System.out.println(signerDN + " dest: " + dstDN);
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<loginTicketRequest version=\"1.0\">"
				+ "<header>" +
				// "<source>" + signerDN + "</source>" + si se omiten estos 2 tags, se habilita
				// al signer
				// "<destination>" + dstDN + "</destination>" +
				"<uniqueId>" + (currentTime / 1000) + "</uniqueId>" + "<generationTime>" + genTime + "</generationTime>"
				+ "<expirationTime>" + expTime + "</expirationTime>" + "</header>" + "<service>" + service
				+ "</service>" + "</loginTicketRequest>";
	}

	private static String invokeWSAA(byte[] cmsData, String endpoint) throws Exception {
		String base64Cms = Base64.getEncoder().encodeToString(cmsData);

		String soapRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
				+ " xmlns:wsaa=\"https://wsaa.afip.gov.ar/ws/services/LoginCms\">" + "<soapenv:Header/>"
				+ "<soapenv:Body>" + "<wsaa:loginCms>" + "<wsaa:in0>" + base64Cms + "</wsaa:in0>" + "</wsaa:loginCms>"
				+ "</soapenv:Body>" + "</soapenv:Envelope>";

		System.out.println("Enviando solicitud SOAP a: " + endpoint);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(endpoint);
		httpPost.setHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.setHeader("SOAPAction", "\"loginCms\"");
		httpPost.setEntity(new StringEntity(soapRequest, "UTF-8"));

		HttpResponse response = httpClient.execute(httpPost);
		System.out.println("Respuesta HTTP: " + response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Error en respuesta: " + responseText);
			throw new Exception("Error HTTP: " + response.getStatusLine().getStatusCode());
		}

		return responseText;
	}

	private static AfipAuth processResponse(String response) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document soapDoc = db.parse(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));

		NodeList returnNodes = soapDoc.getElementsByTagName("loginCmsReturn");
		String loginCmsReturn = returnNodes.item(0).getTextContent();

		Document ltrDoc = db.parse(new ByteArrayInputStream(loginCmsReturn.getBytes(StandardCharsets.UTF_8)));

		String token = ltrDoc.getElementsByTagName("token").item(0).getTextContent();
		String sign = ltrDoc.getElementsByTagName("sign").item(0).getTextContent();
		String expTime = ltrDoc.getElementsByTagName("expirationTime").item(0).getTextContent();

		LocalDateTime expiration = LocalDateTime.parse(expTime.substring(0, 19)); // parse básico ISO-like

		System.out.println("TOKEN: " + token);
		System.out.println("SIGN: " + sign);
		System.out.println("EXPIRATION: " + expiration);

		return new AfipAuth(token, sign, expiration);
	}
}