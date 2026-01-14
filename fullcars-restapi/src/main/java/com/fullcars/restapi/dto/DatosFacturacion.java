package com.fullcars.restapi.dto;
import com.fullcars.restapi.facturacion.enums.Conceptos;
import com.fullcars.restapi.facturacion.enums.IvaAlicuota;
import com.fullcars.restapi.facturacion.enums.TipoDocumento;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;

/**
 * Encapsula todos los datos de facturación que no están en el modelo 'Sale'.
 */
public class DatosFacturacion {

	// NOTA: Asumimos una única alícuota para toda la factura,
	private final IvaAlicuota alicuota = IvaAlicuota.IVA_21;
	private final Conceptos concepto = Conceptos.PRODUCTOS; 
	private final int puntoVenta = 00003;

	private long cuitEmisor; // Ej: 20228291820

	private TiposComprobante tipoComprobante; 
	// Datos del Comprador
	private TipoDocumento tipoDocumento; 
	private long numeroDocumentoComprador; // Ej: 0 (para Cons. Final)

	public DatosFacturacion() {}
	public DatosFacturacion(long cuitEmisor, TiposComprobante codigoComprobante, long numeroDocumentoComprador) {
		this.cuitEmisor = cuitEmisor;
		this.tipoComprobante = codigoComprobante;
		if(codigoComprobante == TiposComprobante.FACTURA_A) {
			this.tipoDocumento = TipoDocumento.CUIT;
			this.numeroDocumentoComprador = numeroDocumentoComprador;
		}else {
			this.tipoDocumento = TipoDocumento.CONSUMIDOR_FINAL;
			this.numeroDocumentoComprador = 0;
		}
		//this.tipoDocumento = (codigoComprobante == TiposComprobante.FACTURA_A) ? TipoDocumento.CUIT : TipoDocumento.CONSUMIDOR_FINAL;
	}

	public long getCuitEmisor() {
		return cuitEmisor;
	}

	public int getPuntoVenta() {
		return puntoVenta;
	}

	public TiposComprobante getTipoComprobante() {
		return tipoComprobante;
	}

	public Conceptos getConcepto() {
		return concepto;
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public long getNumeroDocumento() {
		return numeroDocumentoComprador;
	}

	public IvaAlicuota getAlicuota() {
		return alicuota;
	}
}
