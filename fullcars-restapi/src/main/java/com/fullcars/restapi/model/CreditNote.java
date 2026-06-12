package com.fullcars.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@DiscriminatorValue("CREDIT_NOTE")
public class CreditNote extends Comprobante {

	@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_asociado_id")
    private Factura comprobanteAsociado;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_credit_id")
    private CustomerCredit customerCredit;

    @JsonInclude
    public String getComprobanteAsociadoToPDF() {
        return "Factura A Nro. " + String.format("%05d", comprobanteAsociado.getPuntoVenta()) + "-" + String.format("%08d", comprobanteAsociado.getNumeroComprobante());
    }
    
    public Factura getComprobanteAsociado() {
        return comprobanteAsociado;
    }

    public void setComprobanteAsociado(Factura comprobanteAsociado) {
        this.comprobanteAsociado = comprobanteAsociado;
    }

    public CustomerCredit getCustomerCredit() {
        return customerCredit;
    }

    public void setCustomerCredit(CustomerCredit customerCredit) {
        this.customerCredit = customerCredit;
    }

	@Override
	public String getTextoTitulo() {
		return "NOTA DE CRÉDITO";
	}
}
