package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.CustomerCredit;

@Repository
public interface ICustomerCreditRepository extends JpaRepository<CustomerCredit, Long> {
}
