package com.sgd_hc.sgd_hc.module_expedientes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ExpedientePublicoResponse(
        Long id,
        String numeroExpediente,
        Long patientId,
        String patientName,
        String patientDocument,
        String estado,
        LocalDate fechaApertura,
        LocalDateTime fechaUltimaModificacion
) {}
