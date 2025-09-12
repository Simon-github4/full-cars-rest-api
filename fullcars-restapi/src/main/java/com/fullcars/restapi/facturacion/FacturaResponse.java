package com.fullcars.restapi.facturacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacturaResponse {
    private String cae;
    private String vencimiento;
    private Long numeroComprobante;

    public FacturaResponse(String cae, String vencimiento, Long numeroComprobante) {
        this.cae = cae;
        this.vencimiento = vencimiento;
        this.numeroComprobante = numeroComprobante;
    }

}
