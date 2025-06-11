package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Provider;

public interface IProviderRepository extends JpaRepository<Provider, Long>{

}
