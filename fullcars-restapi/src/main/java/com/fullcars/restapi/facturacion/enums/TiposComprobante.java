package com.fullcars.restapi.facturacion.enums;

public enum TiposComprobante {
    FACTURA_A(1), FACTURA_B(6), FACTURA_C(11);

    private final int codigo;

    private TiposComprobante(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
    public char getTipo() {
    			switch (this) {
			case FACTURA_A:
				return 'A';
			case FACTURA_B:
				return 'B';
			case FACTURA_C:
				return 'C';
			default:
				throw new IllegalArgumentException("Tipo de Comprobante inválido: " + this);
		}
    }
    
    public static TiposComprobante fromCodigo(int codigo) {
        for (TiposComprobante c : TiposComprobante.values()) 
            if (c.codigo == codigo) 
                return c;
            
        throw new IllegalArgumentException("Código de Tipo de Comprobante inválido: " + codigo);
    }
    
}
