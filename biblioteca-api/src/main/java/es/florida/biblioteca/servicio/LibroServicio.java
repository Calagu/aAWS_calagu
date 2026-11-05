package es.florida.biblioteca.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.florida.biblioteca.dao.AutorDao;
import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;

@Service
public class LibroServicio {

    private final LibroDao libroDao;
    private final AutorDao autorDao;

    public LibroServicio(LibroDao libroDao, AutorDao autorDao) {
        this.libroDao = libroDao;
        this.autorDao = autorDao;
    }

    public List<Libro> buscar(String titulo, String genero, Integer autorId) {
        return libroDao.buscar(vacioANulo(titulo), vacioANulo(genero), autorId);
    }

    public Libro obtener(int id) {
        return libroDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro " + id));
    }

    @Transactional
    public Libro crear(LibroPeticion datos) {
        comprobarAutor(datos.autorId());
        if (libroDao.existeIsbn(datos.isbn(), null)) {
            throw new ConflictoException("Ya existe un libro con el ISBN " + datos.isbn());
        }
        int id = libroDao.insertar(datos);
        return obtener(id);
    }

    @Transactional
    public Libro actualizar(int id, LibroPeticion datos) {
        // Se bloquea el libro: ningun prestamo concurrente puede cambiar los abiertos mientras se comprueba.
        libroDao.bloquearEjemplares(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro " + id));
        comprobarAutor(datos.autorId());
        if (libroDao.existeIsbn(datos.isbn(), id)) {
            throw new ConflictoException("Ya existe otro libro con el ISBN " + datos.isbn());
        }
        int abiertos = libroDao.contarPrestamosAbiertos(id);
        if (datos.ejemplares() < abiertos) {
            throw new ConflictoException("El libro " + id + " tiene " + abiertos
                    + " préstamos abiertos: no puede tener menos ejemplares");
        }
        libroDao.actualizar(id, datos);
        return obtener(id);
    }

    @Transactional
    public void borrar(int id) {
        obtener(id);
        if (libroDao.contarPrestamos(id) > 0) {
            throw new ConflictoException("No se puede borrar el libro " + id + ": ya se ha prestado");
        }
        libroDao.borrar(id);
    }

    private void comprobarAutor(int autorId) {
        if (autorDao.buscarPorId(autorId).isEmpty()) {
            throw new DatosInvalidosException("El autor " + autorId + " no existe");
        }
    }

    private static String vacioANulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
