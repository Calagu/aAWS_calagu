package es.florida.biblioteca.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import es.florida.biblioteca.modelo.Prestamo;

public interface PrestamoDao {

    /** estado: null (todos), ACTIVO, VENCIDO o DEVUELTO. */
    List<Prestamo> buscar(String estado, LocalDate hoy);

    List<Prestamo> buscarPorSocio(int socioId, LocalDate hoy);

    Optional<Prestamo> buscarPorId(int id, LocalDate hoy);

    /** SELECT ... FOR UPDATE sobre el prestamo. */
    Optional<Prestamo> bloquearPorId(int id, LocalDate hoy);

    int insertar(int libroId, int socioId, LocalDate fechaPrestamo, LocalDate fechaLimite);

    int registrarDevolucion(int id, LocalDate fecha);

    int contarVencidosDeSocio(int socioId, LocalDate hoy);

    int contarAbiertosDeSocio(int socioId);

    boolean tieneAbiertoDelLibro(int socioId, int libroId);

    int contarAbiertosDeLibro(int libroId);
}
