package com.fullcars.restapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@Table(name = "purchase_details")
public class PurchaseDetail extends BaseDetail{

	private static final String TABLE_NAME = "purchase_details";

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}
}