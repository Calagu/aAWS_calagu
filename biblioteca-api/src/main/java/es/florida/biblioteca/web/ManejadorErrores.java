package es.florida.biblioteca.web;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;

/**
 * UNICO punto donde se generan las respuestas de error, con el formato del apartado 7.3.
 * Extiende ResponseEntityExceptionHandler para capturar tambien los errores propios de Spring MVC
 * (JSON mal formado, tipo de parametro incorrecto, ruta inexistente, metodo no permitido, 415...).
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final Clock reloj;

    public ManejadorErrores(Clock reloj) {
        this.reloj = reloj;
    }

    // ---------- Errores de la aplicacion ----------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest peticion) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), peticion.getRequestURI(), List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRespuesta> conflicto(ConflictoException ex, HttpServletRequest peticion) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), peticion.getRequestURI(), List.of());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(DatosInvalidosException ex, HttpServletRequest peticion) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), peticion.getRequestURI(), List.of());
    }

    // ---------- Base de datos ----------

    /** BD caida o sin conexiones: el cliente no ha hecho nada mal, el servidor no puede atenderle (503, no 500). */
    @ExceptionHandler({ DataAccessResourceFailureException.class, TransientDataAccessResourceException.class,
            CannotCreateTransactionException.class })
    public ResponseEntity<ErrorRespuesta> baseDeDatosNoDisponible(Exception ex, HttpServletRequest peticion) {
        LOG.warn("Base de datos no disponible: {}", ex.getMessage());
        return construir(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio no puede atender la petición en este momento: base de datos no disponible",
                peticion.getRequestURI(), List.of());
    }

    /** Respaldo ante una carrera: la restriccion UNIQUE de la BD salta antes que la comprobacion previa. */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorRespuesta> claveDuplicada(DuplicateKeyException ex, HttpServletRequest peticion) {
        return construir(HttpStatus.CONFLICT, "Ya existe un registro con ese valor único",
                peticion.getRequestURI(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> integridad(DataIntegrityViolationException ex, HttpServletRequest peticion) {
        LOG.warn("Violacion de integridad: {}", ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.BAD_REQUEST, "Los datos no cumplen las restricciones de la base de datos",
                peticion.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception ex, HttpServletRequest peticion) {
        LOG.error("Error inesperado en {}", peticion.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                peticion.getRequestURI(), List.of());
    }

    // ---------- Errores de Spring MVC (se reescriben al formato comun) ----------

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object cuerpo, HttpHeaders cabeceras,
            HttpStatusCode estado, WebRequest peticion) {
        String mensaje = switch (estado.value()) {
            case 404 -> "Recurso no encontrado";
            case 405 -> "Método HTTP no permitido para esta ruta";
            case 406 -> "Formato de respuesta no soportado";
            case 415 -> "Tipo de contenido no soportado: usa Content-Type: application/json";
            default -> estado.is5xxServerError() ? "Error interno del servidor" : "Petición no válida";
        };
        return ResponseEntity.status(estado).headers(cabeceras)
                .body(cuerpo(estado, mensaje, ruta(peticion), List.of()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders cabeceras, HttpStatusCode estado, WebRequest peticion) {
        // Una entrada por campo incorrecto, ordenadas por nombre de campo
        Map<String, String> porCampo = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            porCampo.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        List<String> detalles = porCampo.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
        return ResponseEntity.status(estado).headers(cabeceras)
                .body(cuerpo(estado, "La petición contiene datos no válidos", ruta(peticion), detalles));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders cabeceras, HttpStatusCode estado, WebRequest peticion) {
        return ResponseEntity.status(estado).headers(cabeceras)
                .body(cuerpo(estado, "El cuerpo de la petición no es un JSON válido", ruta(peticion), List.of()));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders cabeceras,
            HttpStatusCode estado, WebRequest peticion) {
        String nombre = (ex instanceof MethodArgumentTypeMismatchException m) ? m.getName() : ex.getPropertyName();
        return ResponseEntity.status(estado).headers(cabeceras)
                .body(cuerpo(estado, "El valor '" + ex.getValue() + "' no es válido para el parámetro '" + nombre + "'",
                        ruta(peticion), List.of()));
    }

    // ---------- Utilidades ----------

    private ResponseEntity<ErrorRespuesta> construir(HttpStatusCode estado, String mensaje, String ruta,
            List<String> detalles) {
        return ResponseEntity.status(estado).body(cuerpo(estado, mensaje, ruta, detalles));
    }

    private ErrorRespuesta cuerpo(HttpStatusCode estado, String mensaje, String ruta, List<String> detalles) {
        HttpStatus http = HttpStatus.resolve(estado.value());
        String texto = (http != null) ? http.getReasonPhrase() : "Error";
        return new ErrorRespuesta(OffsetDateTime.now(reloj).format(FORMATO), estado.value(), texto, mensaje, ruta,
                detalles);
    }

    private static String ruta(WebRequest peticion) {
        return (peticion instanceof ServletWebRequest sw) ? sw.getRequest().getRequestURI() : "";
    }
}
