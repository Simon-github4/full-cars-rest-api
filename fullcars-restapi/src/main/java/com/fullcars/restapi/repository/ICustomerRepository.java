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



}
