package com.sgd_hc.documents.mapper;

import com.sgd_hc.documents.dto.DicomStudyResponseDto;
import com.sgd_hc.documents.entity.Document;
import org.springframework.stereotype.Component;



@Component
public class DicomMapper {
    /**
     * Convierte un {@link Document} de categoría DICOM_STUDY en su DTO de respuesta.
     *
     * @param doc entidad Document ya persistida (con tenant, patient y uploader cargados)
     * @return DTO listo para serializar a JSON y enviar al cliente
     */
    public DicomStudyResponseDto toResponseDto(Document doc) {
        String patientName  = doc.getPatient().getFirstName()  + " " + doc.getPatient().getLastName();
        String uploaderName = doc.getUploader().getFirstName() + " " + doc.getUploader().getLastName();

        return new DicomStudyResponseDto(
                doc.getId(),
                doc.getPatient().getId(),
                patientName,
                doc.getUploader().getId(),
                uploaderName,
                doc.getFileUrl(),
                doc.getIssueDate(),
                doc.getStatus(),
                doc.getCategory()
        );
    }
}
