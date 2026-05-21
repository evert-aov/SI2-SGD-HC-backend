package com.sgd_hc.documents.dto;

import com.sgd_hc.documents.entity.DocumentCategory;
import com.sgd_hc.documents.entity.DocumentStatus;

import java.time.LocalDate;
import java.util.UUID;



/**
 * DTO de salida para un estudio DICOM.
 *
 * <p>Proyección reducida de {@link com.sgd_hc.documents.entity.Document}
 * enfocada en los campos relevantes para el visor de imágenes médicas.
 *
 * @param id            UUID del documento (se usa para construir la URL de streaming).
 * @param patientId     UUID del paciente propietario del estudio.
 * @param patientName   Nombre completo del paciente (para mostrar en el visor).
 * @param uploaderId    UUID del usuario que subió el archivo.
 * @param uploaderName  Nombre completo del médico/administrativo que lo subió.
 * @param fileUrl       Ruta relativa del archivo en el servidor (ej: /uploads/uuid.dcm).
 * @param issueDate     Fecha de emisión del estudio.
 * @param status        Estado del documento (DRAFT, COMPLETED, etc.).
 * @param category      Siempre será DICOM_STUDY para este DTO.
 */

public record DicomStudyResponseDto(
    UUID id,
    UUID patientId,
    String patientName,
    UUID uploaderId,
    String uploaderName,
    String fileUrl,
    LocalDate issueDate,
    DocumentStatus status,
    DocumentCategory category
) {}
