package com.sgd_hc.sgd_hc.module_expedientes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ExpedienteCompletoResponse(
        Long id,
        String numeroExpediente,
        Long patientId,
        String patientName,
        String patientDocument,
        String patientEmail,
        String patientPhone,
        String estado,
        LocalDate fechaApertura,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaModificacion,
        
        // Datos Sensibles
        String diagnostico,
        String tratamiento,
        String observaciones,
        String antecedentesMedicos,
        Long medicoId,
        String medicoName
) {}
