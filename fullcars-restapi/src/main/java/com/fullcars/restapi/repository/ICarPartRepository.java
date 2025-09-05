package com.fullcars.restapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.dto.TopProductDTO;
import com.fullcars.restapi.model.CarPart;
@Repository
public interface ICarPartRepository extends JpaRepository<CarPart, Long>{

    Optional<CarPart> findBySku(String sku);

    List<CarPart> findByStockLessThan(Long stockThreshold);

    @Query("""
    	    SELECT new com.fullcars.restapi.dto.TopProductDTO(
    	        cp.sku,
    	        cp.name,
    	        SUM(sd.quantity),
    	        SUM(sd.quantity * sd.unitPrice)
    	    )
    	    FROM SaleDetail sd
    	    JOIN sd.carPart cp
    	    GROUP BY cp.id, cp.sku, cp.name
    	    ORDER BY SUM(sd.quantity) DESC
    	""")
    	List<TopProductDTO> findTopProducts(Pageable pageable);


}
