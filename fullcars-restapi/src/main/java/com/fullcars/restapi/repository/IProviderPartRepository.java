package com.fullcars.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.ProviderMapping;
import com.fullcars.restapi.model.ProviderPart;

public interface IProviderPartRepository extends JpaRepository<ProviderPart, Long> {
    void deleteByProviderMapping(ProviderMapping mapping);
    List<ProviderPart> findByProviderMapping(ProviderMapping mapping);
}
