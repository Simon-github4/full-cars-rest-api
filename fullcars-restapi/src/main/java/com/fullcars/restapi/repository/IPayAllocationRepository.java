package com.fullcars.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.PayAllocation;

@Repository
public interface IPayAllocationRepository extends JpaRepository<PayAllocation, Long> {
    List<PayAllocation> findBySaleId(Long saleId);
    List<PayAllocation> findByPaymentSplitId(Long paymentSplitId);
    
    @Query("SELECT a FROM PayAllocation a WHERE a.paymentSplit.id = :splitId")
    List<PayAllocation> findBySplitId(@Param("splitId") Long splitId);
}
