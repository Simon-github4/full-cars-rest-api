package com.fullcars.restapi.dto;
public class CAEResponse {

	private String cae;
	private String fechaVencimiento;
	private long numeroComprobante;
	private String observaciones;
	private String error;

	// Getters y Setters
	public String getCae() {
		return cae;
	}

	public void setCae(String cae) {
		this.cae = cae;
	}

	public String getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public long getNumeroComprobante() {
		return numeroComprobante;
	}

	public void setNumeroComprobante(long numeroComprobante) {
		this.numeroComprobante = numeroComprobante;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		if (error != null) {
			return "Error: " + error;
		}
		return "CAEResponse [" + "cae=" + cae + ", " + "fechaVencimiento=" + fechaVencimiento + ", "
				+ "numeroComprobante=" + numeroComprobante
				+ (observaciones != null ? ", observaciones=" + observaciones : "") + "]";
	}
}