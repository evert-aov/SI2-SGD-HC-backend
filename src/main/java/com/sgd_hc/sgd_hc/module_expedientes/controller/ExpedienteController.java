package com.sgd_hc.sgd_hc.module_expedientes.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedienteCompletoResponse;
import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedientePublicoResponse;
import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedienteSearchRequest;
import com.sgd_hc.sgd_hc.module_expedientes.service.ExpedienteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/module_expedientes/expedientes")
@RequiredArgsConstructor
public class ExpedienteController {

    private final ExpedienteService expedienteService;

    // ─── Endpoints Públicos (Búsqueda Web / Sin Login) ──────────────────────────

    @GetMapping("/public/search")
    public ResponseEntity<ExpedientePublicoResponse> searchPublic(
            @RequestParam("numeroExpediente") String numeroExpediente) {
        Optional<ExpedientePublicoResponse> response = expedienteService.searchPublicByNumero(numeroExpediente);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ─── Endpoints Autenticados (Con Login / Angular & Flutter App) ─────────────

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ExpedienteCompletoResponse>> searchPrivate(
            ExpedienteSearchRequest request) {
        return ResponseEntity.ok(expedienteService.searchPrivate(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpedienteCompletoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expedienteService.getExpedienteById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpedienteCompletoResponse> create(
            @RequestParam("patientId") Long patientId,
            @RequestParam(value = "diagnostico", required = false) String diagnostico,
            @RequestParam(value = "tratamiento", required = false) String tratamiento,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "antecedentes", required = false) String antecedentes) {
        
        ExpedienteCompletoResponse response = expedienteService.createExpediente(
                patientId, diagnostico, tratamiento, observaciones, antecedentes);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpedienteCompletoResponse> update(
            @PathVariable Long id,
            @RequestParam(value = "diagnostico", required = false) String diagnostico,
            @RequestParam(value = "tratamiento", required = false) String tratamiento,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "antecedentes", required = false) String antecedentes,
            @RequestParam(value = "estado", required = false) String estado) {
        
        ExpedienteCompletoResponse response = expedienteService.updateExpediente(
                id, diagnostico, tratamiento, observaciones, antecedentes, estado);
        return ResponseEntity.ok(response);
    }
}
