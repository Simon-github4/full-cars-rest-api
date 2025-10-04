package com.fullcars.restapi.facturacion;

import org.springframework.stereotype.Component;

@Component
public class FacturaClientFactory {

    private final WsfeClient wsfeClient;
    private final WsmtxcaClient wsmtxcaClient;

    public FacturaClientFactory(WsfeClient wsfeClient, WsmtxcaClient wsmtxcaClient) {
        this.wsfeClient = wsfeClient;
        this.wsmtxcaClient = wsmtxcaClient;
    }

    public FacturaClient getClient() {
        //if (tipoC.equals(TiposComprobante.)) {
        return wsmtxcaClient; // por ahora solo usaran este
        //return wsfeClient;
    }
}
