package com.fullcars.restapi.facturacion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

