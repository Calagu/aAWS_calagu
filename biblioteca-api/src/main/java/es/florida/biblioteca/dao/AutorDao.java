package es.florida.biblioteca.dao;

import java.util.List;
import java.util.Optional;

import es.florida.biblioteca.modelo.Autor;
import es.florida.biblioteca.modelo.AutorPeticion;

public interface AutorDao {

    List<Autor> buscarTodos();

    Optional<Autor> buscarPorId(int id);

    /** Inserta y devuelve el id generado por la base de datos. */
    int insertar(AutorPeticion datos);

    int actualizar(int id, AutorPeticion datos);

    int borrar(int id);

    int contarLibros(int autorId);
}
