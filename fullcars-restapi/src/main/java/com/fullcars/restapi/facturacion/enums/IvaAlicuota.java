package com.fullcars.restapi.facturacion.enums;

import java.math.BigDecimal;

public enum IvaAlicuota {
        // Códigos de AFIP: 3=0%, 4=10.5%, 5=21%, 6=27%
        IVA_21(5, new BigDecimal("0.21")),
        IVA_10_5(4, new BigDecimal("0.105")),
        IVA_27(6, new BigDecimal("0.27")),
        IVA_0(3, new BigDecimal("0.0"));

        private final int codigo;
        private final BigDecimal multiplicador;

        IvaAlicuota(int codigo, BigDecimal multiplicador) {
            this.codigo = codigo;
            this.multiplicador = multiplicador;
        }

        public int getCodigo() { return codigo; }
        public BigDecimal getMultiplicador() { return multiplicador; }

}