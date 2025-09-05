package com.fullcars.restapi.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Customer;
@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Long>{

    Optional<Customer> findByDni(String dni);

    @Query("""
    	    SELECT COALESCE(SUM(sd.unitPrice * sd.quantity), 0) - COALESCE(SUM(p.amount), 0)
    	    FROM SaleDetail sd
    	    LEFT JOIN sd.sale s
    	    LEFT JOIN Pay p ON p.customer = s.customer
    	""")
    BigDecimal getTotalToChargeAll();


}
