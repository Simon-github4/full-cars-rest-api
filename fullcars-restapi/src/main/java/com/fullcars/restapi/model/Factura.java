package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fullcars.restapi.facturacion.enums.Conceptos;
import com.fullcars.restapi.facturacion.enums.CondicionIva;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonBackReference
	@OneToOne
	private Sale sale;
    private String fileUrl;
	
    @Column(name = "cuit_emisor", nullable = false)
    private Long cuitEmisor;
    
	@Column(name = "punto_venta", nullable = false)
    private Integer puntoVenta;

    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private Integer tipoComprobante;//obt. codigo del ENUM TiposComprobante

    @Column(name = "numero_comprobante", nullable = false)
    private Long numeroComprobante;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Enumerated(EnumType.STRING) 
    @Column(name = "concepto", nullable = false) 
    private Conceptos concepto;

    // --- 3. Datos del Receptor (Foto del cliente al momento de facturar) ---
    // IMPORTANTE: Se guardan aquí para historizar. Si el cliente cambia de dirección mañana,
    // esta factura antigua no debe cambiar.
    
    @Column(name = "cuit_cliente", nullable = false)
    private Long cuitCliente;

    @Column(name = "tipo_doc_cliente", nullable = false)
    private Integer tipoDocCliente; // 80, 96, 99 (obt. del ENUM TipoDocumento)

    @Column(name = "razon_social_cliente", nullable = false, length = 200)
    private String razonSocialCliente;

    @Column(name = "domicilio_cliente", length = 250)
    private String domicilioCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_iva_cliente", length = 50)
    private CondicionIva condicionIvaCliente;

    // --- 4. Importes Fiscales (Precisión para dinero) ---
    // DECIMAL(19, 2) es el estándar para importes
    
    @Column(name = "imp_neto", precision = 19, scale = 2)
    private BigDecimal impNeto; // Gravado

    /*@Column(name = "imp_no_gravado", precision = 19, scale = 2)
    private BigDecimal impNoGravado;

    @Column(name = "imp_exento", precision = 19, scale = 2)
    private BigDecimal impExento;*/

    @Column(name = "imp_iva", precision = 19, scale = 2)
    private BigDecimal impIva;

    @Column(name = "imp_tributos", precision = 19, scale = 2)
    private BigDecimal impTributos = BigDecimal.ZERO; // IIBB, etc.

    @Column(name = "imp_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal impTotal;

    // --- 5. Respuesta AFIP (Validación Legal) ---
    @Column(name = "cae", length = 20)
    private String cae;

    @Column(name = "vto_cae")
    private LocalDate vtoCae;

    @Column(name = "resultado_afip", length = 1) // "A", "R", "P"
    private String resultadoAfip;

    @Column(name = "observaciones_afip", length = 500)
    private String observaciones;

    @Column(name = "fecha_vto_pago")
    private LocalDate fechaVencimientoPago;
}
