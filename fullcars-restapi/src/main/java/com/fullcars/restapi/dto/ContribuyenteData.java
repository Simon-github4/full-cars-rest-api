package com.fullcars.restapi.dto;

import com.fullcars.restapi.facturacion.enums.CondicionIva;

public class ContribuyenteData {
    private String nombre;
    private String direccion;
    private String localidad;
    private String provincia;
    private String codigoPostal;
    private CondicionIva condicionIva; 

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public CondicionIva getCondicionIva() {
        return condicionIva;
    }

    public void setCondicionIva(CondicionIva condicionIva) {
        this.condicionIva = condicionIva;
    }

    @Override
    public String toString() {
        return "ContribuyenteData [nombre=" + nombre + ", direccion=" + direccion + ", localidad=" + localidad
                + ", provincia=" + provincia + ", codigoPostal=" + codigoPostal + ", condicionIva=" + condicionIva.getDescripcion()
                + "]";
    }
}