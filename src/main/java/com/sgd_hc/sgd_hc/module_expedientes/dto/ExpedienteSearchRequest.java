package com.sgd_hc.sgd_hc.module_expedientes.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpedienteSearchRequest {
    private String numeroExpediente;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long patientId;
    private String keyword; // For text search in diagnosis/treatment
    
    // Pagination defaults
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
}
