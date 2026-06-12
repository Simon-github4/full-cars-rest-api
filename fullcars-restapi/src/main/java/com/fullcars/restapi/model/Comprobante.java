package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullcars.restapi.facturacion.enums.Conceptos;
import com.fullcars.restapi.facturacion.enums.CondicionIva;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
//@Table(name = "comprobante")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "comprobante_type", length=20, discriminatorType = DiscriminatorType.STRING)
public abstract class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileUrl;

    @Column(name = "cuit_emisor", nullable = false)
    private Long cuitEmisor;

    @Column(name = "punto_venta", nullable = false)
    private Integer puntoVenta;

    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private Integer tipoComprobante;

    @Column(name = "numero_comprobante", nullable = false)
    private Long numeroComprobante;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(name = "concepto", nullable = false)
    private Conceptos concepto;

    @Column(name = "cuit_cliente", nullable = false)
    private Long cuitCliente;

    @Column(name = "tipo_doc_cliente", nullable = false)
    private Integer tipoDocCliente;

    @Column(name = "razon_social_cliente", nullable = false, length = 200)
    private String razonSocialCliente;

    @Column(name = "domicilio_cliente", length = 250)
    private String domicilioCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_iva_cliente", length = 50)
    private CondicionIva condicionIvaCliente;

    @Column(name = "imp_neto", precision = 19, scale = 2)
    private BigDecimal impNeto;

    @Column(name = "imp_iva", precision = 19, scale = 2)
    private BigDecimal impIva;

    @Column(name = "imp_tributos", precision = 19, scale = 2)
    private BigDecimal impTributos = BigDecimal.ZERO;

    @Column(name = "imp_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal impTotal;

    @Column(name = "cae", length = 20)
    private String cae;

    @Column(name = "vto_cae")
    private LocalDate vtoCae;

    @Column(name = "resultado_afip", length = 1)
    private String resultadoAfip;

    @Column(name = "observaciones_afip", length = 500)
    private String observaciones;

    @Column(name = "fecha_vto_pago")
    private LocalDate fechaVencimientoPago;
    
    @JsonIgnore
    public abstract String getTextoTitulo();

	public abstract String getComprobanteAsociadoToPDF();//PARA MAPEAR AL PDF DINAMICO DESDE COMPROBANTE
}
