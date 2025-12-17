package com.fullcars.restapi.facturacion;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fullcars.restapi.facturacion.enums.TiposComprobante;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    /*@PostMapping("/{saleId}")
    public ResponseEntity<FacturaResponse> facturar(@PathVariable Long saleId, @RequestParam TiposComprobante tipoC) {
        FacturaResponse response = facturaService.emitirFactura(saleId, tipoC);
        return ResponseEntity.ok(response);
    }*/
    
    @GetMapping
    public String showData() {
    	return facturaService.getAfipData();
    }
    
    @GetMapping("/emitir")
    public ResponseEntity<?> emitirFactura(
            @RequestParam Long idVenta,
            @RequestParam int tipoComprobante,
            @RequestParam Long idReceptor) {
        try {
            byte[] pdfBytes = facturaService.generarFactura(idVenta, TiposComprobante.fromCodigo(tipoComprobante), idReceptor);

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
}

