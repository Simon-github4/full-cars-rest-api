package com.fullcars.restapi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;

public interface IFacturaRepository extends JpaRepository<Factura, Long>{


	public void deleteBySaleId(Long idSale);
	
	public Optional<Factura> findBySaleId(Long idSale);
	
    //public List<Factura> findByDateBetween(LocalDate start, LocalDate end);

}
