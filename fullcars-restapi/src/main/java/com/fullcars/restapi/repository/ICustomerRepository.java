package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Customer;

public interface ICustomerRepository extends JpaRepository<Customer, Long>{

}
