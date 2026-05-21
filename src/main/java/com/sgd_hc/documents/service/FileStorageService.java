package com.sgd_hc.documents.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${storage.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Guarda el archivo en disco y retorna la URL relativa pública.
     * El nombre es UUID + nombre original para evitar colisiones.
     */
    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = original.contains(".")
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + extension;

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        Path dest = dir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        log.info(">>> FileStorage: archivo guardado en {}", dest);
        return "/uploads/" + filename;
    }


    /**
     * Carga un archivo desde disco y lo devuelve como un {@link Resource}
     * listo para ser transmitido al cliente (streaming HTTP).
     */
    public Resource loadAsResource(String relativeUrl) throws IOException {

        // 1. Reconstruimos la ruta absoluta en disco a partir de la URL relativa.
        //    relativeUrl viene como "/uploads/abc123.dcm"
        String filename = relativeUrl.startsWith("/uploads/")
                ? relativeUrl.substring("/uploads/".length())
                : relativeUrl;

        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);

        
        // 2. Verificamos que el archivo exista antes de intentar abrirlo.
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException(
                    "Archivo no encontrado en el servidor: " + filename);
        }


        // 3. Creamos el Resource. UrlResource acepta un URI (file://ruta/absoluta).
        //    Spring usará este Resource para abrir el InputStream cuando el ResponseEntity lo necesite.
        Resource resource = new UrlResource(filePath.toUri());

        log.info(">>> FileStorage: sirviendo archivo {}", filePath);
        return resource;
    }
}
