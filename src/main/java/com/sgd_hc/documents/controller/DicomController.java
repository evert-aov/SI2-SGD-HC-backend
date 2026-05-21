package com.sgd_hc.documents.controller;

import com.sgd_hc.documents.dto.DicomStudyResponseDto;
import com.sgd_hc.documents.dto.DicomUploadRequestDto;
import com.sgd_hc.documents.service.DicomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


/**
 * Controlador REST para la gestión de estudios DICOM.
 *
 * Todos los endpoints requieren autenticación JWT y permisos específicos.
 * El aislamiento multi-tenant lo aplica el DicomService internamente.
 */
@RestController
@RequestMapping("/api/dicom")
@RequiredArgsConstructor
public class DicomController {
    
    private final DicomService dicomService;


    // =========================================================================
    // ENDPOINT 1: Subir un estudio DICOM
    // POST /api/dicom/upload

    /**
     * Recibe un archivo .dcm junto con sus metadatos y lo registra en el sistema.
     * <p>La petición debe ser {@code multipart/form-data} con tres partes:
     * <ul>
     *   <li>{@code file}      — el archivo DICOM binario</li>
     *   <li>{@code patientId} — UUID del paciente</li>
     *   <li>{@code issueDate} — fecha del estudio (formato ISO: YYYY-MM-DD)</li>
     * </ul>
     *
     * @return 201 Created con el DTO del estudio recién creado.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DICOM_CREATE')")
    public ResponseEntity<DicomStudyResponseDto> upload(
            @RequestPart("file")      MultipartFile file,
            @RequestPart("patientId") String patientId,
            @RequestPart("issueDate") String issueDate   
    ) throws IOException {

        DicomUploadRequestDto dto = new DicomUploadRequestDto(
            UUID.fromString(patientId),   
            LocalDate.parse(issueDate)
            // patientId,
            // LocalDate.parse(issueDate)
        );

        DicomStudyResponseDto response = dicomService.uploadDicomStudy(file, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // =========================================================================
    // ENDPOINT 2: Obtener metadatos de un estudio por ID
    // GET /api/dicom/{id}

    /**
     * Retorna los metadatos de un estudio DICOM específico.
     *
     * <p>Satisface el CA-4 (restricción de acceso): si el estudio no existe,
     * pertenece a otro tenant o el documento no es de tipo DICOM_STUDY,
     * el servicio lanza {@link jakarta.persistence.EntityNotFoundException}
     * y el cliente recibe un 404.
     *
     * @param id UUID del estudio DICOM
     * @return 200 OK con los metadatos del estudio.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DICOM_READ')")
    public ResponseEntity<DicomStudyResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(dicomService.getStudyById(id));
    }


    // =========================================================================
    // ENDPOINT 3: Streaming del archivo DICOM
    // GET /api/dicom/{id}/stream

    /**
     * Transmite el archivo DICOM como un stream de bytes al cliente.
     *
     * <p>El header {@code Content-Type: application/dicom} es obligatorio para
     * que librerías como Cornerstone.js puedan interpretar los bytes correctamente.
     *
     * <p>Satisface el CA-1 (apertura del estudio) y CA-4 (acceso solo a usuarios
     * autorizados): la verificación de tenant ocurre en el servicio antes de
     * leer el disco.
     *
     * @param id UUID del estudio DICOM
     * @return 200 OK con el stream de bytes del archivo .dcm
     */
    @GetMapping("/{id}/stream")
    @PreAuthorize("hasAuthority('DICOM_READ')")
    public ResponseEntity<Resource> streamDicom(@PathVariable UUID id) throws IOException {
        Resource resource = dicomService.loadDicomAsResource(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/dicom"))
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }


    // =========================================================================
    // ENDPOINT 4: Listar estudios DICOM de un paciente
    // GET /api/dicom/patient/{patientId}

    /**
     * Retorna todos los estudios DICOM asociados a un paciente dentro del
     * tenant actual. La lista puede estar vacía si el paciente aún no tiene
     * estudios registrados.
     *
     * @param patientId UUID del paciente
     * @return 200 OK con la lista de estudios (puede ser vacía []).
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('DICOM_READ')")
    public ResponseEntity<List<DicomStudyResponseDto>> getByPatient(
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(dicomService.getStudiesByPatient(patientId));
    }

}
