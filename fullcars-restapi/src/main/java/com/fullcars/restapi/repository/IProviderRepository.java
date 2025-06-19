package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Provider;
@Repository
public interface IProviderRepository extends JpaRepository<Provider, Long>{

}
