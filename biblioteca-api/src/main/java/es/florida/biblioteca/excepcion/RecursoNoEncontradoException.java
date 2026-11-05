package es.florida.biblioteca.excepcion;

/** Se lanza cuando un recurso no existe (404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
