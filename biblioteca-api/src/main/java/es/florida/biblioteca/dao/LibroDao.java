package es.florida.biblioteca.dao;

import java.util.List;
import java.util.Optional;

import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;

public interface LibroDao {

    /** Filtros opcionales y combinables; un filtro null no se aplica. */
    List<Libro> buscar(String titulo, String genero, Integer autorId);

    Optional<Libro> buscarPorId(int id);

    /** SELECT ... FOR UPDATE: bloquea la fila del libro y devuelve sus ejemplares. */
    Optional<Integer> bloquearEjemplares(int id);

    int insertar(LibroPeticion datos);

    int actualizar(int id, LibroPeticion datos);

    int borrar(int id);

    /** Indica si el ISBN ya existe en otro libro (excluirId null = en cualquiera). */
    boolean existeIsbn(String isbn, Integer excluirId);

    int contarPrestamosAbiertos(int libroId);

    int contarPrestamos(int libroId);
}
