package com.sgd_hc.documents.service;

import com.sgd_hc.documents.dto.DicomStudyResponseDto;
import com.sgd_hc.documents.dto.DicomUploadRequestDto;
import com.sgd_hc.documents.entity.Document;
import com.sgd_hc.documents.entity.DocumentCategory;
import com.sgd_hc.documents.entity.DocumentStatus;
import com.sgd_hc.documents.mapper.DicomMapper;
import com.sgd_hc.documents.repository.DocumentRepository;
import com.sgd_hc.patients.entity.Patient;
import com.sgd_hc.patients.repository.PatientRepository;
import com.sgd_hc.security.details.SecurityUser;
import com.sgd_hc.tenants.entity.Tenant;
import com.sgd_hc.tenants.service.TenantResolverService;
import com.sgd_hc.users.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


/**
 * Servicio de negocio para estudios DICOM.
 *
 * <p>Gestiona la subida, consulta y streaming de archivos de imagen médica
 * en formato DICOM (.dcm), aislados por tenant (clínica).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DicomService {
    
    //  Extensión válida para archivos DICOM 
    private static final String DICOM_EXTENSION = ".dcm";

    private final DocumentRepository   documentRepository;
    private final PatientRepository    patientRepository;
    private final DicomMapper          dicomMapper;
    private final FileStorageService   fileStorageService;
    private final TenantResolverService tenantResolverService;


    @Transactional
    public DicomStudyResponseDto uploadDicomStudy(MultipartFile file,
                                                   DicomUploadRequestDto dto) throws IOException {
        // ── 1. Validar extensión 
        // Obtenemos el nombre original del archivo que envió el cliente.
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        if (!originalFilename.endsWith(DICOM_EXTENSION)) {
            throw new IllegalArgumentException(
                    "Solo se permiten archivos DICOM (.dcm). Archivo recibido: "
                    + file.getOriginalFilename());
        }

        
        // ── 2. Resolver tenant 
        // TenantResolverService lee el UUID del tenant del TenantContext,
        // que fue colocado ahí por el filtro JWT al autenticar la petición.
        Tenant tenant = tenantResolverService.resolve();


        // ── 3. Buscar paciente 
        // El paciente debe existir. No verificamos que sea del mismo tenant
        // aquí porque PatientRepository tiene el filtro de tenant activo via
        // Hibernate Filter. Si el paciente no es del tenant, no lo encontrará.
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Paciente no encontrado con id: " + dto.patientId()));


        // ── 4. Guardar archivo en disco 
        // FileStorageService guarda el archivo con un nombre UUID único y
        // devuelve la ruta relativa: "/uploads/abc123.dcm"
        String fileUrl = fileStorageService.store(file);


        // ── 5. Crear y persistir el documento 
        Document doc = new Document();
        doc.setTenant(tenant);
        doc.setPatient(patient);
        doc.setUploader(currentUser());
        doc.setFileUrl(fileUrl);
        doc.setIssueDate(dto.issueDate());
        doc.setIsExternalSource(true);             
        doc.setCategory(DocumentCategory.DICOM_STUDY); 
        doc.setStatus(DocumentStatus.COMPLETED);   

        Document saved = documentRepository.save(doc);
        log.info(">>> DicomService: estudio guardado con id={} para paciente={}",
                saved.getId(), patient.getId());

        return dicomMapper.toResponseDto(saved);
    }


    // OPERACIÓN 2: Obtener metadatos de un estudio por su ID
    /**
     * Retorna los metadatos de un estudio DICOM específico.
     *
     * <p>La búsqueda filtra por id + tenant + categoría DICOM_STUDY.
     * Si el id pertenece a un documento no-DICOM o de otro tenant,
     * lanza {@link EntityNotFoundException} en lugar de devolver datos.
     * Esto satisface el CA-4 (restricción de acceso).
     *
     * @param id UUID del estudio DICOM
     * @return DTO con los metadatos del estudio
     */
    @Transactional(readOnly = true)
    public DicomStudyResponseDto getStudyById(UUID id) {
        Tenant tenant = tenantResolverService.resolve();

        Document doc = documentRepository
                .findByIdAndTenantIdAndCategory(id, tenant.getId(), DocumentCategory.DICOM_STUDY)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estudio DICOM no encontrado con id: " + id));

        return dicomMapper.toResponseDto(doc);
    }


    // OPERACIÓN 3: Listar todos los estudios DICOM de un paciente
    /**
     * Retorna todos los estudios DICOM de un paciente dentro del tenant actual.
     *
     * @param patientId UUID del paciente
     * @return lista de DTOs (puede ser vacía si el paciente no tiene estudios)
     */
    @Transactional(readOnly = true)
    public List<DicomStudyResponseDto> getStudiesByPatient(UUID patientId) {
        Tenant tenant = tenantResolverService.resolve();

        return documentRepository
                .findByPatientIdAndTenantIdAndCategory(
                        patientId, tenant.getId(), DocumentCategory.DICOM_STUDY)
                .stream()
                .map(dicomMapper::toResponseDto)
                .toList();
    }


    // OPERACIÓN 4: Cargar el archivo .dcm desde disco para streaming
    /**
     * Carga el archivo DICOM físico desde disco y lo devuelve como un
     * {@link Resource} listo para ser transmitido al cliente.
     *
     * <p>Primero verifica que el estudio existe y pertenece al tenant actual
     * (seguridad), luego delega la lectura del disco a FileStorageService.
     *
     * @param id UUID del estudio DICOM
     * @return Resource apuntando al archivo .dcm en disco
     * @throws IOException si falla la lectura del archivo
     */
    @Transactional(readOnly = true)
    public Resource loadDicomAsResource(UUID id) throws IOException {
        // Primero verificamos que el estudio existe en BD para este tenant.
        // Esto aplica la restricción de acceso ANTES de leer el disco.
        DicomStudyResponseDto study = getStudyById(id);

        // Delegamos la carga del archivo físico al FileStorageService.
        return fileStorageService.loadAsResource(study.fileUrl());
    }


    // Helper privado: obtener el usuario autenticado actual
    /**
     * Extrae el {@link User} del contexto de seguridad de Spring.
     *
     */
    private User currentUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof SecurityUser su) {
            return su.getUser();
        }
        throw new IllegalStateException("No hay usuario autenticado en el contexto");
    }

}
