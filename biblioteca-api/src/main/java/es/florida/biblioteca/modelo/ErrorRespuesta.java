package es.florida.biblioteca.modelo;

import java.util.List;

/** Formato unico de error de la API (apartado 7.3 del enunciado). */
public record ErrorRespuesta(String marcaTemporal, int estado, String error, String mensaje,
        String ruta, List<String> detalles) {
}
