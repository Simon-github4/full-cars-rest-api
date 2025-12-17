package com.fullcars.restapi.facturacion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fullcars.restapi.dto.CAEResponse;
import com.fullcars.restapi.dto.ContribuyenteData;
import com.fullcars.restapi.dto.DatosFacturacion;
import com.fullcars.restapi.facturacion.enums.IvaAlicuota;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;
import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.model.SaleDetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class FacturaPDFGenerator {

    // Ruta donde guardaste el archivo compilado por JasperStudio dentro de 'resources'
    private static final String REPORT_A = "reports/facturaA.jasper";
    private static final String REPORT_B = "reports/facturaB.jasper";

    public static byte[] generarFacturaPDF(Factura fact, IvaAlicuota iva) throws JRException, IOException {

        // 1. Cargar la plantilla compilada (.jasper) desde resources
        // Usamos ClassPathResource para que funcione bien dentro del JAR en producción
    	ClassPathResource resource = new ClassPathResource(REPORT_A);
        
        if (!resource.exists()) 
            throw new FileNotFoundException("¡ERROR FATAL! No encuentro el archivo: " + REPORT_A);

        try (InputStream reportStream = resource.getInputStream()) {

        	Map<String, Object> parameters = mapearDtoAParametros(fact);
	
	        JRDataSource dataSource = new JRBeanCollectionDataSource(mapearDetalles(fact.getSale(), iva));
	
	        JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, dataSource);
	        System.out.println(">>> 4. REPORTE LLENO. PÁGINAS GENERADAS: " + jasperPrint.getPages().size());

	        return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }

    /**
     * Mapea manualmente los campos del DTO a las claves que espera JasperReport.
     * Los nombres en "put" deben ser idénticos a los parámetros creados en JasperStudio.
     * @throws IOException 
     */
    private static  Map<String, Object> mapearDtoAParametros(Factura fact) throws IOException {
    	Map<String, Object> params = new HashMap<>();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    	params.put("LOGO_IMG", new ClassPathResource("static/logo-arca.jpg").getInputStream());
        

        // Cabecera y Datos AFIP
        params.put("FECHA_EMISION", fact.getFechaEmision().format(formatter));
        params.put("PUNTO_VENTA", String.format("%05d", fact.getPuntoVenta()));
        // Formateamos el número de comprobante a 8 dígitos con ceros a la izquierda
        params.put("COMP_NRO", String.format("%08d", fact.getNumeroComprobante()));
        params.put("COD_FACT", fact.getTipoComprobante());
        params.put("TIPO_FACT", TiposComprobante.fromCodigo(fact.getTipoComprobante()).getTipo());
        params.put("CAE", fact.getCae());
        params.put("FECHA_VTO_CAE", fact.getVtoCae().format(formatter));

        // Emisor
        params.put("CUIT_EMISOR", String.valueOf(fact.getCuitEmisor()));
        //params.put("RAZON_SOCIAL_EMISOR", dto.getRazonSocialEmisor());
        //params.put("DOMICILIO_EMISOR", dto.getDomicilioEmisor());
        //params.put("FECHA_INICIO_ACT", dto.getFechaInicioAct());

        // Cliente
        params.put("CUIT_CLIENTE", String.valueOf(fact.getCuitCliente()));
        params.put("RAZON_SOCIAL_CLIENTE", fact.getRazonSocialCliente());
        params.put("DOMICILIO_CLIENTE", fact.getDomicilioCliente());
        params.put("CONDICION_IVA_CLIENTE", fact.getCondicionIvaCliente().getDescripcion());
        params.put("CONDICION_VENTA", "Cuenta Corriente");

        // Totales (Pasan como BigDecimal, Jasper sabe formatearlos como moneda)
        params.put("IMPORTE_NETO", fact.getImpNeto());
        params.put("IVA_21", fact.getImpIva());
        params.put("IMPORTE_TOTAL", fact.getImpTotal());

        // Parámetro opcional para imágenes (logo) si lo necesitas a futuro
        // params.put("LOGO_DIR", new ClassPathResource("img/logo.png").getPath());

        return params;
    }
    
    private static List<DetalleFacturaDto> mapearDetalles(Sale sale, IvaAlicuota ivaAlicuota) {
        List<DetalleFacturaDto> listaDetalles = new ArrayList<>();

        for (SaleDetail item : sale.getDetails()) { // Ajusta 'SaleDetail' a tu clase real
            
            // Cálculos por ítem (Ajusta según tu lógica de negocio)
            BigDecimal precio = item.getUnitPrice();
            Integer cantidad = item.getQuantity(); // Asegúrate que sea BigDecimal
            BigDecimal subtotal = item.getSubTotal();
            
            // Creamos el DTO del detalle
            DetalleFacturaDto detalle = new DetalleFacturaDto(
                item.getCarPart().getName(), // Nombre producto
                cantidad,
                precio,
                BigDecimal.ZERO, // Bonificación (si tienes)
                subtotal,        // Subtotal
                ivaAlicuota.getMultiplicador().multiply(BigDecimal.valueOf(100)).toString()+"%",// Alicuota (puedes sacarla del producto si varía)
                subtotal.add(ivaAlicuota.getMultiplicador().multiply(subtotal)) // Subtotal con IVA
            );

            listaDetalles.add(detalle);
        }
        return listaDetalles;
    }
    
    @Data
    @AllArgsConstructor
    public static class DetalleFacturaDto {
        private String producto;        // Nombre o descripción
        private Integer cantidad;    // Puede ser Integer si no vendes por peso
        private BigDecimal precioUnitario;
        private BigDecimal bonificacion; // Descuento si aplica
        private BigDecimal subtotal;     // (cant * precio) - bonif
        private String alicuota;        // Ej: "21%" o "10.5%"
        private BigDecimal subtotalConIva;
    }
    
}

