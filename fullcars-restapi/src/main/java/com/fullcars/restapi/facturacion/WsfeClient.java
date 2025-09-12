package com.fullcars.restapi.facturacion;

import org.springframework.stereotype.Component;

import com.fullcars.restapi.model.Sale;

@Component
public class WsfeClient implements FacturaClient {

    private final ArcaSoapClient soapClient;

    public WsfeClient(ArcaSoapClient soapClient) {
        this.soapClient = soapClient;
    }
    /*
    @Override
    public FacturaResponse solicitarCAE(Sale sale) {
        // Mapear Sale → FECAEDetRequest
        FECAEDetRequest request = FacturaMapper.toFECAEDetRequest(sale);

        FECAEResponse response = soapClient.callFECAESolicitar(request);

        return new FacturaResponse(
                response.getCAE(),
                response.getCAEFchVto(),
                response.getCbteDesde()
        );
    }
    */

	@Override
	public FacturaResponse solicitarCAE(Sale sale) {
		// TODO Auto-generated method stub
		return null;
	}
}

