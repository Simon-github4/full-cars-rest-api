package com.fullcars.restapi.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Pay;
@Repository
public interface IPayRepository extends JpaRepository<Pay, Long>{

	List<Pay> findByCustomerId(Long customerId);

    @Query("""
    	    SELECT COALESCE(SUM(p.amount), 0)
    	    FROM Pay p
    	""")
    BigDecimal calculateTotalPayments();
    
}
