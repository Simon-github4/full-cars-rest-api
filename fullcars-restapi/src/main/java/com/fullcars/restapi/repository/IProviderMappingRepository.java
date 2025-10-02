package com.fullcars.restapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.ProviderMapping;
@Repository
public interface IProviderMappingRepository extends JpaRepository<ProviderMapping, Long> {
    Optional<ProviderMapping> findByProviderId(Long providerId);
}

