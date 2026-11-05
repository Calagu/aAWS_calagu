package es.florida.biblioteca.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import es.florida.biblioteca.modelo.Socio;
import es.florida.biblioteca.modelo.SocioPeticion;

public interface SocioDao {

    List<Socio> buscarTodos();

    Optional<Socio> buscarPorId(int id);

    /** SELECT ... FOR UPDATE: bloquea la fila del socio. Devuelve false si no existe. */
    boolean bloquearPorId(int id);

    int insertar(SocioPeticion datos, LocalDate fechaAlta);

    int actualizar(int id, SocioPeticion datos);

    int borrar(int id);

    /** Indica si el email ya existe en otro socio (excluirId null = en cualquiera). */
    boolean existeEmail(String email, Integer excluirId);

    int contarPrestamos(int socioId);
}
