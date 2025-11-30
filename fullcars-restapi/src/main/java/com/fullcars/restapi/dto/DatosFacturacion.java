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
	private TipoDocumento tipoDocumento; // Ej: 96 (Cons. Final), 80 (CUIT)
	private long numeroDocumento; // Ej: 0 (para Cons. Final)

	public DatosFacturacion(long cuitEmisor, TiposComprobante codigoComprobante, TipoDocumento codigoTipoDocumento, long numeroDocumento) {
		this.cuitEmisor = cuitEmisor;
		this.tipoComprobante = codigoComprobante;
		this.tipoDocumento = codigoTipoDocumento;
		this.numeroDocumento = numeroDocumento;
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
		return numeroDocumento;
	}

	public IvaAlicuota getAlicuota() {
		return alicuota;
	}
}
