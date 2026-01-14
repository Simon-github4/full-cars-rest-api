package com.fullcars.restapi.repository;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;

public interface IFacturaRepository extends JpaRepository<Factura, Long>{


	public void deleteBySaleId(Long idSale);
	
	public Optional<Factura> findBySaleId(Long idSale);

	@Query("SELECT f.fileUrl FROM Factura f WHERE f.sale.id = :saleId")
	public String findFilePathBySaleId(@Param("saleId")Long saleId);
	
    //public List<Factura> findByDateBetween(LocalDate start, LocalDate end);

}
