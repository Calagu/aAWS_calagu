package es.florida.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion externalizada (prefijo "biblioteca").
 * Se puede sobrescribir con variables de entorno: BIBLIOTECA_ALUMNO,
 * BIBLIOTECA_PRESTAMO_MAXIMO y BIBLIOTECA_PRESTAMO_DIAS.
 */
@ConfigurationProperties(prefix = "biblioteca")
public record BibliotecaProperties(String alumno, ReglasPrestamo prestamo) {

    public record ReglasPrestamo(int maximo, int dias) {
    }
}
