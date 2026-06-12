package com.fullcars.restapi.facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.dto.CAEResponse;
import com.fullcars.restapi.dto.DatosFacturacion;
import com.fullcars.restapi.facturacion.enums.IvaAlicuota;
import com.fullcars.restapi.facturacion.enums.TipoDocumento;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;
import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;

public class WSFEV1Service extends WSFEV1Client {

    /*public static CAEResponse generarCAE(AfipAuth auth, Sale sale, DatosFacturacion datos, long ultimoComp,
            String endpoint, String service) throws Exception {
        return generarCAE(auth, sale, datos, ultimoComp, endpoint, service, null, null);
    }

    public static CAEResponse generarCAE(AfipAuth auth, Sale sale, DatosFacturacion datos, long ultimoComp,
            String endpoint, String service, Factura comprobanteAsociado) throws Exception {
        return generarCAE(auth, sale, datos, ultimoComp, endpoint, service, comprobanteAsociado, null);
    }*/

    public static CAEResponse generarCAE(AfipAuth auth, Sale sale, DatosFacturacion datos, long ultimoComp,
            String endpoint, String service, Factura comprobanteAsociado, BigDecimal totalOverride) throws Exception {
        long proximoNumero = ultimoComp + 1;
        System.out.println("Proximo numero a autorizar: " + proximoNumero);

        String soapRequest = buildFECAESolicitarRequest(auth, sale, datos, proximoNumero, comprobanteAsociado, totalOverride);
        System.out.println("Enviando solicitud de autorizacion (WSFEV1)..." + soapRequest);

        String soapResponse = invokeWS(soapRequest, "FECAESolicitar", endpoint, service);
        System.out.println("Respuesta recibida." + soapResponse);

        return parseFECAESolicitarResponse(soapResponse, proximoNumero);
    }

    private static String buildFECAESolicitarRequest(AfipAuth auth, Sale sale, DatosFacturacion datos,
            long numeroComprobante, Factura comprobanteAsociado, BigDecimal totalOverride) {

        IvaAlicuota alicuota = datos.getAlicuota();

        BigDecimal totalComprobante = (totalOverride != null)
                ? totalOverride.setScale(2, RoundingMode.HALF_UP)
                : sale.getTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal divisor = BigDecimal.ONE.add(alicuota.getMultiplicador());
        BigDecimal netoTotal = totalComprobante.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal ivaTotal = totalComprobante.subtract(netoTotal);

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        long codigoCondicionIvaReceptor;
        TiposComprobante tipoComprobante = datos.getTipoComprobante();
        if (tipoComprobante == TiposComprobante.FACTURA_A || tipoComprobante == TiposComprobante.FACTURA_B) {
            codigoCondicionIvaReceptor = (tipoComprobante == TiposComprobante.FACTURA_A) ? 1L : 5L;
        } else {
            codigoCondicionIvaReceptor = (datos.getTipoDocumento() == TipoDocumento.CUIT) ? 1L : 5L;
        }

        String cbtesAsocXml = "";
        if (comprobanteAsociado != null) {// es NC
            cbtesAsocXml = String.format(
                    "<ar:CbtesAsoc>"
                            + "<ar:CbteAsoc>"
                            + "<ar:Tipo>%d</ar:Tipo>"
                            + "<ar:PtoVta>%d</ar:PtoVta>"
                            + "<ar:Nro>%d</ar:Nro>"
                            + "<ar:Cuit>%d</ar:Cuit>"
                            + "<ar:CbteFch>%s</ar:CbteFch>"
                            + "</ar:CbteAsoc>"
                            + "</ar:CbtesAsoc>",
                    comprobanteAsociado.getTipoComprobante(),
                    comprobanteAsociado.getPuntoVenta(),
                    comprobanteAsociado.getNumeroComprobante(),
                    comprobanteAsociado.getCuitEmisor(),
                    comprobanteAsociado.getFechaEmision().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }

        String ivaXml = String.format(
                "<ar:Iva>"
                        + "<ar:AlicIva>"
                        + "<ar:Id>%d</ar:Id>"
                        + "<ar:BaseImp>%s</ar:BaseImp>"
                        + "<ar:Importe>%s</ar:Importe>"
                        + "</ar:AlicIva>"
                        + "</ar:Iva>",
                alicuota.getCodigo(), netoTotal.toString(), ivaTotal.toString());

        String soapTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ar=\"http://ar.gov.afip.dif.FEV1/\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<ar:FECAESolicitar>"
                + "<ar:Auth>"
                + "<ar:Token>%s</ar:Token>"
                + "<ar:Sign>%s</ar:Sign>"
                + "<ar:Cuit>%d</ar:Cuit>"
                + "</ar:Auth>"
                + "<ar:FeCAEReq>"
                + "<ar:FeCabReq>"
                + "<ar:CantReg>1</ar:CantReg>"
                + "<ar:PtoVta>%d</ar:PtoVta>"
                + "<ar:CbteTipo>%d</ar:CbteTipo>"
                + "</ar:FeCabReq>"
                + "<ar:FeDetReq>"
                + "<ar:FECAEDetRequest>"
                + "<ar:Concepto>%d</ar:Concepto>"
                + "<ar:DocTipo>%d</ar:DocTipo>"
                + "<ar:DocNro>%d</ar:DocNro>"
                + "<ar:CondicionIVAReceptorId>%d</ar:CondicionIVAReceptorId>"
                + "<ar:CbteDesde>%d</ar:CbteDesde>"
                + "<ar:CbteHasta>%d</ar:CbteHasta>"
                + "<ar:CbteFch>%s</ar:CbteFch>"
                + "<ar:ImpTotal>%s</ar:ImpTotal>"
                + "<ar:ImpTotConc>0</ar:ImpTotConc>"
                + "<ar:ImpNeto>%s</ar:ImpNeto>"
                + "<ar:ImpOpEx>0</ar:ImpOpEx>"
                + "<ar:ImpTrib>0</ar:ImpTrib>"
                + "<ar:ImpIVA>%s</ar:ImpIVA>"
                + "<ar:MonId>PES</ar:MonId>"
                + "<ar:MonCotiz>1</ar:MonCotiz>"
                + "%s"
                + "%s"
                + "</ar:FECAEDetRequest>"
                + "</ar:FeDetReq>"
                + "</ar:FeCAEReq>"
                + "</ar:FECAESolicitar>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";

        return String.format(soapTemplate, auth.getToken(), auth.getSign(), datos.getCuitEmisor(),
                datos.getPuntoVenta(), datos.getTipoComprobante().getCodigo(), datos.getConcepto().codigo(),
                datos.getTipoDocumento().getCodigo(), datos.getNumeroDocumento(), codigoCondicionIvaReceptor,
                numeroComprobante, numeroComprobante, fecha, totalComprobante.toString(), netoTotal.toString(),
                ivaTotal.toString(), cbtesAsocXml, ivaXml);
    }

    private static CAEResponse parseFECAESolicitarResponse(String soapResponse, long numeroComprobanteAsignado)
            throws Exception {
        CAEResponse response = new CAEResponse();
        Document doc = parseXml(soapResponse);

        NodeList errors = doc.getElementsByTagName("Err");
        if (errors.getLength() > 0) {
            String msg = getTagValue(errors.item(0), "Msg");
            response.setError(msg != null ? msg : "Error desconocido de AFIP.");
            return response;
        }

        NodeList detResponse = doc.getElementsByTagName("FECAEDetResponse");
        if (detResponse.getLength() > 0) {
            Node detNode = detResponse.item(0);

            String resultado = getTagValue(detNode, "Resultado");

            if ("A".equals(resultado)) {
                response.setCae(getTagValue(detNode, "CAE"));
                response.setFechaVencimiento(getTagValue(detNode, "CAEFchVto"));
                response.setNumeroComprobante(numeroComprobanteAsignado);

                NodeList obs = doc.getElementsByTagName("Observaciones");
                if (obs.getLength() > 0 && obs.item(0).hasChildNodes()) {
                    response.setObservaciones(getTagValue(obs.item(0).getFirstChild(), "Msg"));
                }
                return response;
            }

            NodeList obs = doc.getElementsByTagName("Observaciones");
            if (obs.getLength() > 0 && obs.item(0).hasChildNodes()) {
                response.setError("Rechazado: " + getTagValue(obs.item(0).getFirstChild(), "Msg"));
            } else {
                response.setError("Rechazado por AFIP (sin observaciones).");
            }
            return response;
        }

        response.setError("Respuesta SOAP no reconocida.");
        return response;
    }
}
