package com.fullcars.restapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.CreditNote;

@Repository
public interface ICreditNoteRepository extends JpaRepository<CreditNote, Long> {

    Optional<CreditNote> findFirstByComprobanteAsociadoIdOrderByIdDesc(Long comprobanteAsociadoId);

    List<CreditNote> findAllByComprobanteAsociadoIdOrderByIdDesc(Long comprobanteAsociadoId);
}
