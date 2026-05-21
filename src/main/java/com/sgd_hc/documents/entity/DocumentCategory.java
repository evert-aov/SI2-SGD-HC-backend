package com.sgd_hc.documents.entity;

/**
 * Categoría funcional de un documento clínico.
 *
 * <p>Espejo del tipo {@code document_category_enum} definido en PostgreSQL
 * (migración V7). El nombre de cada constante debe coincidir exactamente
 * con el valor del ENUM en la base de datos.
 *
 */


public enum DocumentCategory {
    CLINICAL_FORM,
    EXTERNAL_FILE,
    DICOM_STUDY
}
