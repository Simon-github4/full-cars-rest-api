package com.fullcars.restapi.facturacion;

import org.springframework.stereotype.Component;

import com.fullcars.restapi.model.Sale;

@Component
public class WsmtxcaClient implements FacturaClient {

    private final ArcaSoapClient soapClient;

    public WsmtxcaClient(ArcaSoapClient soapClient) {
        this.soapClient = soapClient;
    }

    @Override
    public FacturaResponse solicitarCAE(Sale sale) {
        // Mapear Sale → CmpDetalleType (WSMTXCA)
        CmpDetalleType detalle = FacturaMapper.toCmpDetalleType(sale);

        CmpResponse response = soapClient.callMTXCA(detalle);

        return new FacturaResponse(
                response.getCae(),
                response.getFchVto(),
                response.getNroComprobante()
        );
    }
}

