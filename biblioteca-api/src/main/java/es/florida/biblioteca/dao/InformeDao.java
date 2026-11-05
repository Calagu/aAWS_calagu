package es.florida.biblioteca.dao;

import java.util.List;

import es.florida.biblioteca.modelo.LibroMasPrestado;

/** DAO del informe de la variante 1. */
public interface InformeDao {

    List<LibroMasPrestado> masPrestados(int limite);
}
