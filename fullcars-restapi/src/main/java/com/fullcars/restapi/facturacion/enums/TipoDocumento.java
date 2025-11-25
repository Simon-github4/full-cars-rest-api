package com.fullcars.restapi.facturacion.enums;

public enum TipoDocumento {
    CUIT(80, "CUIT"),
    CUIL(86, "CUIL"),
    CDI(87, "CDI"),
    LE(89, "Libreta de Enrolamiento"),
    LC(90, "Libreta Cívica"),
    DNI(96, "Documento Nacional de Identidad"),
    PASAPORTE(94, "Pasaporte"),
    CI_EXTRANJERA(91, "Cédula de Identidad Extranjera"),
    CONSUMIDOR_FINAL(99, "Consumidor Final");

    private final int codigo;
    private final String descripcion;

    TipoDocumento(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion + " (" + codigo + ")";
    }
}
