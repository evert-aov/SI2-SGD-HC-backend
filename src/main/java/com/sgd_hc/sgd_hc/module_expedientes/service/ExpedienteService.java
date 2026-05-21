package com.sgd_hc.sgd_hc.module_expedientes.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedienteCompletoResponse;
import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedientePublicoResponse;
import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedienteSearchRequest;
import com.sgd_hc.sgd_hc.module_expedientes.entity.Expediente;
import com.sgd_hc.sgd_hc.module_expedientes.mapper.ExpedienteMapper;
import com.sgd_hc.sgd_hc.module_expedientes.repository.ExpedienteRepository;
import com.sgd_hc.sgd_hc.module_patients.entity.Patient;
import com.sgd_hc.sgd_hc.module_patients.repository.PatientRepository;
import com.sgd_hc.sgd_hc.module_users.entity.User;
import com.sgd_hc.sgd_hc.module_users.repository.UserRepository;
import com.sgd_hc.sgd_hc.security.details.SecurityUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpedienteService {

    private final ExpedienteRepository expedienteRepository;
    private final PatientRepository    patientRepository;
    private final UserRepository       userRepository;
    private final ExpedienteMapper     expedienteMapper;

    // ─── Búsqueda Pública (Sin Login / Búsqueda Web General) ────────────────────
    
    @Transactional(readOnly = true)
    public Optional<ExpedientePublicoResponse> searchPublicByNumero(String numeroExpediente) {
        if (numeroExpediente == null || numeroExpediente.isBlank()) {
            throw new IllegalArgumentException("El número de expediente es requerido para búsquedas públicas.");
        }
        return expedienteRepository.findByNumeroExpediente(numeroExpediente)
                .map(expedienteMapper::toPublicResponse);
    }

    // ─── Búsqueda Privada (Con Login - Diferenciación de Roles) ──────────────────

    @Transactional(readOnly = true)
    public Page<ExpedienteCompletoResponse> searchPrivate(ExpedienteSearchRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }

        // Aplicación del control de actores en filtros dinámicos
        if (!isStaffUser(currentUser)) {
            // El paciente común SOLO puede buscar y acceder a sus propios expedientes.
            // Forzamos que el ID del paciente sea el del usuario actual autenticado.
            request.setPatientId(currentUser.getId());
        }

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "fechaUltimaModificacion")
        );

        Page<Expediente> expedientes = expedienteRepository.searchExpedientes(
                request.getNumeroExpediente(),
                request.getEstado(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getPatientId(),
                request.getKeyword(),
                pageable
        );

        return expedientes.map(expedienteMapper::toCompletoResponse);
    }

    @Transactional(readOnly = true)
    public ExpedienteCompletoResponse getExpedienteById(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }

        Expediente expediente = findOrThrow(id);

        // Control de Actores: Si no es Staff, verificar si el expediente le pertenece
        if (!isStaffUser(currentUser)) {
            if (expediente.getPatient() == null || !expediente.getPatient().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Acceso denegado: No tienes permisos para ver expedientes clínicos ajenos.");
            }
        }

        return expedienteMapper.toCompletoResponse(expediente);
    }

    // ─── Creación y Modificación (Exclusivo Staff / Médicos) ─────────────────────

    @Transactional
    public ExpedienteCompletoResponse createExpediente(Long patientId, String diagnostico, String tratamiento, String observaciones, String antecedentes) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !isStaffUser(currentUser)) {
            throw new AccessDeniedException("Solo el personal médico o administrativo puede crear expedientes.");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con id: " + patientId));

        String numeroExpediente = generateNumeroExpediente();

        Expediente expediente = Expediente.builder()
                .numeroExpediente(numeroExpediente)
                .patient(patient)
                .estado("ACTIVO")
                .fechaApertura(LocalDate.now())
                .diagnostico(diagnostico)
                .tratamiento(tratamiento)
                .observaciones(observaciones)
                .antecedentesMedicos(antecedentes)
                .medico(currentUser)
                .build();

        return expedienteMapper.toCompletoResponse(expedienteRepository.save(expediente));
    }

    @Transactional
    public ExpedienteCompletoResponse updateExpediente(Long id, String diagnostico, String tratamiento, String observaciones, String antecedentes, String estado) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !isStaffUser(currentUser)) {
            throw new AccessDeniedException("Solo el personal autorizado puede modificar expedientes clínicos.");
        }

        Expediente expediente = findOrThrow(id);
        
        if (diagnostico != null) expediente.setDiagnostico(diagnostico);
        if (tratamiento != null) expediente.setTratamiento(tratamiento);
        if (observaciones != null) expediente.setObservaciones(observaciones);
        if (antecedentes != null) expediente.setAntecedentesMedicos(antecedentes);
        if (estado != null && !estado.isBlank()) expediente.setEstado(estado);

        return expedienteMapper.toCompletoResponse(expedienteRepository.save(expediente));
    }

    // ─── Helpers de Seguridad y Datos ───────────────────────────────────────────

    private Expediente findOrThrow(Long id) {
        return expedienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expediente no encontrado con id: " + id));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUser();
        }
        return null;
    }

    private boolean isStaffUser(User user) {
        if (user == null) return false;
        // Roles administrativos, médicos y de archivo tienen acceso total
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_SUPERUSER")
                        || role.getName().equals("ROLE_ADMIN")
                        || role.getName().equals("ROLE_MEDICO")
                        || role.getName().equals("ROLE_ARCHIVO")
                        || role.getName().equals("ROLE_DIRECTOR"));
    }

    private String generateNumeroExpediente() {
        String prefix = "EXP";
        String code;
        do {
            int n = (int) (Math.random() * 900000) + 100000;
            code = prefix + "-" + n;
        } while (expedienteRepository.existsByNumeroExpediente(code));
        return code;
    }
}
