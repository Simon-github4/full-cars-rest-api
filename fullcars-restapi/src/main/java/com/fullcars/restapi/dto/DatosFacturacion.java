package com.fullcars.restapi.dto;
import com.fullcars.restapi.facturacion.enums.Conceptos;
import com.fullcars.restapi.facturacion.enums.IvaAlicuota;
import com.fullcars.restapi.facturacion.enums.TipoDocumento;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;

/**
 * Encapsula todos los datos de facturación que no están en el modelo 'Sale'.
 */
public class DatosFacturacion {

	// Datos del Emisor
	private long cuitEmisor; // Ej: 20228291820
	private static final int puntoVenta = 00003;

	// Datos del Comprobante
	private TiposComprobante tipoComprobante; // Ej: 6 (Factura B), 1 (Factura A)
	private Conceptos concepto; // 1=Productos, 2=Servicios, 3=Productos y Servicios

	// Datos del Comprador
	private TipoDocumento tipoDocumento; // Ej: 96 (Cons. Final), 80 (CUIT)
	private long numeroDocumento; // Ej: 0 (para Cons. Final)

	// Datos de IVA
	// NOTA: Asumimos una única alícuota para toda la factura,
	// ya que el modelo Sale/CarPart no tiene este dato por ítem.
	private IvaAlicuota alicuota;

	// Constructor
	public DatosFacturacion(long cuitEmisor, TiposComprobante codigoComprobante, Conceptos codigoConcepto,
			TipoDocumento codigoTipoDocumento, long numeroDocumento, IvaAlicuota alicuota) {
		this.cuitEmisor = cuitEmisor;
		this.tipoComprobante = codigoComprobante;
		this.concepto = codigoConcepto;
		this.tipoDocumento = codigoTipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.alicuota = alicuota;
	}

	// Getters
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
