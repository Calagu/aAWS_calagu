package es.florida.biblioteca.excepcion;

/** Se lanza cuando la operacion choca con el estado actual de los datos (409). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
