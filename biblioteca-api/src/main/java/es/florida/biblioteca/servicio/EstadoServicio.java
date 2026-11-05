package es.florida.biblioteca.servicio;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import es.florida.biblioteca.config.BibliotecaProperties;
import es.florida.biblioteca.modelo.Estado;

@Service
public class EstadoServicio {

    private static final long MIB = 1024L * 1024L;

    private final JdbcClient jdbc;
    private final BibliotecaProperties propiedades;

    public EstadoServicio(JdbcClient jdbc, BibliotecaProperties propiedades) {
        this.jdbc = jdbc;
        this.propiedades = propiedades;
    }

    /** Si la base de datos no responde, lanza una excepcion que el manejador convierte en 503. */
    public Estado consultar() {
        String version;
        try {
            version = jdbc.sql("SELECT VERSION()").query(String.class).single();
        } catch (DataAccessException e) {
            throw new DataAccessResourceFailureException("La base de datos no responde", e);
        }
        Runtime jvm = Runtime.getRuntime();
        long reservada = jvm.totalMemory();
        return new Estado(
                "OK",
                propiedades.alumno(),
                new Estado.Parametros(propiedades.prestamo().maximo(), propiedades.prestamo().dias()),
                version,
                new Estado.MemoriaJvm(jvm.maxMemory() / MIB, reservada / MIB, (reservada - jvm.freeMemory()) / MIB));
    }
}
