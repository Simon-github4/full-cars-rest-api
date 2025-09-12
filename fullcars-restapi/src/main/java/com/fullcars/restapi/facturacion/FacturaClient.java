package com.fullcars.restapi.facturacion;

import com.fullcars.restapi.model.Sale;

public interface FacturaClient {
    FacturaResponse solicitarCAE(Sale sale);
}
