package com.sgd_hc.sgd_hc.module_expedientes.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sgd_hc.sgd_hc.module_expedientes.entity.Expediente;

public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {

    boolean existsByNumeroExpediente(String numeroExpediente);
    
    Optional<Expediente> findByNumeroExpediente(String numeroExpediente);

    @Query("SELECT e FROM Expediente e WHERE " +
           "(:numeroExpediente IS NULL OR e.numeroExpediente = :numeroExpediente) AND " +
           "(:estado IS NULL OR e.estado = :estado) AND " +
           "(:fechaInicio IS NULL OR e.fechaApertura >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR e.fechaApertura <= :fechaFin) AND " +
           "(:patientId IS NULL OR e.patient.id = :patientId) AND " +
           "(:keyword IS NULL OR LOWER(e.diagnostico) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.tratamiento) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.observaciones) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.antecedentesMedicos) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Expediente> searchExpedientes(
        @Param("numeroExpediente") String numeroExpediente,
        @Param("estado") String estado,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("patientId") Long patientId,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
