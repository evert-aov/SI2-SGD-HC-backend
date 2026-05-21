package com.sgd_hc.documents.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;


/**
 * DTO de entrada para la subida de un estudio DICOM.
 *
 * <p>Contiene solo los metadatos clínicos necesarios. El archivo físico
 * (.dcm) se recibe como {@code MultipartFile} por separado en el controlador.
 *
 * @param patientId  UUID del paciente al que pertenece el estudio.
 * @param issueDate  Fecha de realización del estudio (ej: fecha de la radiografía).
 */
public record DicomUploadRequestDto(
    @NotNull(message = "El paciente es obligatorio")
    UUID patientId,

    @NotNull(message = "La fecha de emisión es obligatoria")
    LocalDate issueDate
) {}
