package com.fullcars.restapi.facturacion;

public enum TiposComprobante {
    FACTURA_A(1), FACTURA_B(6), FACTURA_C(11);

    private final int codigo;

    private TiposComprobante(int codigo) {
        this.codigo = codigo;
    }

    public int codigo() {
        return codigo;
    }
}
