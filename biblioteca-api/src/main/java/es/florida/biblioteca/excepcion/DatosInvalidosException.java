package es.florida.biblioteca.excepcion;

/** Se lanza cuando los datos de la peticion no son validos por una regla de negocio (400). */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
