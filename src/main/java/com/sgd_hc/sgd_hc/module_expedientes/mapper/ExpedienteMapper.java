package com.sgd_hc.sgd_hc.module_expedientes.mapper;

import org.springframework.stereotype.Component;

import com.sgd_hc.sgd_hc.module_expedientes.entity.Expediente;
import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedientePublicoResponse;
import com.sgd_hc.sgd_hc.module_expedientes.dto.ExpedienteCompletoResponse;

@Component
public class ExpedienteMapper {

    public ExpedientePublicoResponse toPublicResponse(Expediente expediente) {
        if (expediente == null) return null;
        
        String patientName = "";
        String patientDoc = "";
        Long patientId = null;
        
        if (expediente.getPatient() != null) {
            patientId = expediente.getPatient().getId();
            patientDoc = expediente.getPatient().getDocumentNumber();
            patientName = expediente.getPatient().getFirstName() + " " + expediente.getPatient().getLastName();
        }

        return ExpedientePublicoResponse.builder()
                .id(expediente.getId())
                .numeroExpediente(expediente.getNumeroExpediente())
                .patientId(patientId)
                .patientName(patientName.trim())
                .patientDocument(patientDoc)
                .estado(expediente.getEstado())
                .fechaApertura(expediente.getFechaApertura())
                .fechaUltimaModificacion(expediente.getFechaUltimaModificacion())
                .build();
    }

    public ExpedienteCompletoResponse toCompletoResponse(Expediente expediente) {
        if (expediente == null) return null;

        String patientName = "";
        String patientDoc = "";
        String patientEmail = "";
        String patientPhone = "";
        Long patientId = null;
        
        if (expediente.getPatient() != null) {
            patientId = expediente.getPatient().getId();
            patientDoc = expediente.getPatient().getDocumentNumber();
            patientEmail = expediente.getPatient().getEmail();
            patientPhone = expediente.getPatient().getPhone();
            patientName = expediente.getPatient().getFirstName() + " " + expediente.getPatient().getLastName();
        }

        Long medicoId = null;
        String medicoName = "";
        if (expediente.getMedico() != null) {
            medicoId = expediente.getMedico().getId();
            medicoName = expediente.getMedico().getFirstName() + " " + expediente.getMedico().getLastName();
        }

        return ExpedienteCompletoResponse.builder()
                .id(expediente.getId())
                .numeroExpediente(expediente.getNumeroExpediente())
                .patientId(patientId)
                .patientName(patientName.trim())
                .patientDocument(patientDoc)
                .patientEmail(patientEmail)
                .patientPhone(patientPhone)
                .estado(expediente.getEstado())
                .fechaApertura(expediente.getFechaApertura())
                .fechaCreacion(expediente.getFechaCreacion())
                .fechaUltimaModificacion(expediente.getFechaUltimaModificacion())
                .diagnostico(expediente.getDiagnostico())
                .tratamiento(expediente.getTratamiento())
                .observaciones(expediente.getObservaciones())
                .antecedentesMedicos(expediente.getAntecedentesMedicos())
                .medicoId(medicoId)
                .medicoName(medicoName.trim())
                .build();
    }
}
