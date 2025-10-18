package com.fullcars.restapi.repository;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.dto.ProviderPartDTO;
import com.fullcars.restapi.model.ProviderMapping;
import com.fullcars.restapi.model.ProviderPart;

@Repository
public interface IProviderPartRepository extends JpaRepository<ProviderPart, Long> {
    void deleteByProviderMapping(ProviderMapping mapping);
    @Modifying
    @Query("delete from ProviderPart p where p.providerMapping = :mapping")
    void deleteByProviderMappingNative(@Param("mapping") ProviderMapping mapping);
    List<ProviderPart> findByProviderMapping(ProviderMapping mapping);
    @Query("""
            SELECT new com.fullcars.restapi.dto.ProviderPartDTO(
                p.nombre,
                p.marca,
                p.precio,
                p.providerMapping.providerId,
                p.provCod,
                p.category
            )
            FROM ProviderPart p
        """)
        Stream<ProviderPartDTO> streamAllDTOs();
}
