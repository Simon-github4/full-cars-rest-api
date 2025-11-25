package com.fullcars.restapi.facturacion.enums;

public enum Servicios {

    FACTURACION_ELECTRONICA("wsfe"),
    CONSTANCIA_INSCRIPCION("ws_sr_constancia_inscripcion");

    private final String nombre;

    private Servicios(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }


}
