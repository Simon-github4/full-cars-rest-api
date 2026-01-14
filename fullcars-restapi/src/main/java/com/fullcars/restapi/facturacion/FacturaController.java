package com.fullcars.restapi.facturacion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fullcars.restapi.facturacion.enums.TiposComprobante;
import com.fullcars.restapi.model.Factura;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping("/getBySaleId/{saleId}")
    public ResponseEntity<Factura> getBySaleId(@PathVariable Long saleId) {
        //Factura response = facturaService.findBySaleId(saleId);
        return ResponseEntity.ok(facturaService.findBySaleId(saleId));
    }
    
    @GetMapping("/getFacturaPdfBySaleId/{saleId}")
	public ResponseEntity<InputStreamResource> getFacturaPdfBySaleId(@PathVariable Long saleId) throws IOException {
	    File file = facturaService.getFacturaPdf(saleId);
	    
	    String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) 
            mimeType = "application/octet-stream";
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .body(new InputStreamResource(new FileInputStream(file)));
	}
    
    @GetMapping
    public String showData() {
    	return facturaService.getAfipData();
    }
    
    @GetMapping("/emitir")
    public ResponseEntity<?> emitirFactura(
            @RequestParam Long idVenta,
            @RequestParam int tipoComprobante) {
        try {
            byte[] pdfBytes = facturaService.generarFactura(idVenta, TiposComprobante.fromCodigo(tipoComprobante));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF) 
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura_" + idVenta + ".pdf\"")
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace(); 

            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN) // Importante: Texto plano
                    .body(e.getMessage());
        }
    }
    @GetMapping("/isSaleFacturada")
    public boolean isSaleFacturada(@RequestParam Long idSale) {
    	return facturaService.isSaleFacturada(idSale);
    }
}

