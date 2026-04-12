package com.fullcars.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.PayAllocation;

@Repository
public interface IPayAllocationRepository extends JpaRepository<PayAllocation, Long> {
    List<PayAllocation> findByPayId(Long payId);
    List<PayAllocation> findBySaleId(Long saleId);
	void deleteByPayId(Long payId);
    
}
