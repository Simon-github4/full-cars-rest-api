package com.fullcars.restapi.dto;

import com.fullcars.restapi.facturacion.enums.CondicionIva;

public class ContribuyenteData {//PRINCIPALMENTE PARA ARMAR PDF
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
    
    public String getDomicilioComercialFormateado() {
        StringBuilder sb = new StringBuilder();

        // 1. Dirección (Calle y altura)
        if (this.direccion != null && !this.direccion.trim().isEmpty()) {
            sb.append(this.direccion);
        }

        // 2. Localidad (Generalmente separada por un guion)
        if (this.localidad != null && !this.localidad.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(this.localidad);
        }

        // 3. Provincia (Generalmente separada por coma)
        if (this.provincia != null && !this.provincia.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(this.provincia);
        }

        // 4. Código Postal (Opcional: Si querés que aparezca al final)
        /* if (this.codigoPostal != null && !this.codigoPostal.trim().isEmpty()) {
            sb.append(" (CP: ").append(this.codigoPostal).append(")");
        } 
        */

        return sb.toString();
    }
    
}