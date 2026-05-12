package com.fullcars.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.PaymentSplit;

@Repository
public interface IPaymentSplitRepository extends JpaRepository<PaymentSplit, Long> {
    List<PaymentSplit> findByPayId(Long payId);
}