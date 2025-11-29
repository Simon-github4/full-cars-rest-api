package com.fullcars.restapi.facturacion.enums;
import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.dto.CAEResponse;
import com.fullcars.restapi.dto.DatosFacturacion;
import com.fullcars.restapi.model.Sale;

public interface Factura {
	public CAEResponse generarCAE(AfipAuth auth, Sale sale, DatosFacturacion datos, long ultimoComp, String endpoint, String service) throws Exception;
}
