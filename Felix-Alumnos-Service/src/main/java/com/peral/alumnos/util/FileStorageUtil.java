package com.peral.alumnos.util;

import com.peral.alumnos.exception.ArchivoInvalidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {

    @Value("${file.upload-dir:uploads/tareas}")
    private String uploadDir;

    public String guardarArchivo(MultipartFile archivo, Long alumnoId) throws IOException {
        // Crear directorio alumno específico: uploads/tareas/1
        Path directorioAlumno = Paths.get(uploadDir, String.valueOf(alumnoId));
        if (!Files.exists(directorioAlumno)) {
            Files.createDirectories(directorioAlumno);
        }

        // Generar nombre único
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        String nombreUnico = UUID.randomUUID().toString() + extension;
        
        Path rutaCompleta = directorioAlumno.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
        
        // ✅ Guardar solo el nombre del archivo (sin ruta)
        return nombreUnico;
    }
    
    // ✅ CORREGIDO: Ahora recibe el ID del alumno para construir la ruta correcta
    public byte[] cargarArchivo(String nombreArchivo, Long alumnoId) throws IOException {
        Path ruta = Paths.get(uploadDir, String.valueOf(alumnoId), nombreArchivo);
        return Files.readAllBytes(ruta);
    }

    // ✅ CORREGIDO: Ahora recibe el ID del alumno para construir la ruta correcta
    public void eliminarArchivo(String nombreArchivo, Long alumnoId) {
        try {
            Path ruta = Paths.get(uploadDir, String.valueOf(alumnoId), nombreArchivo);
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al eliminar archivo: " + e.getMessage());
        }
    }

    public boolean esPdf(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        String nombreOriginal = archivo.getOriginalFilename();
        
        return contentType != null && 
               (contentType.equals("application/pdf") || 
                nombreOriginal != null && nombreOriginal.toLowerCase().endsWith(".pdf"));
    }
}