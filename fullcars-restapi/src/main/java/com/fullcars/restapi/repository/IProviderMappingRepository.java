package com.fullcars.restapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.ProviderMapping;

public interface IProviderMappingRepository extends JpaRepository<ProviderMapping, Long> {
    Optional<ProviderMapping> findByProviderId(Long providerId);
}

