package es.florida.biblioteca.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.florida.biblioteca.dao.AutorDao;
import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.Autor;
import es.florida.biblioteca.modelo.AutorPeticion;
import es.florida.biblioteca.modelo.Libro;

@Service
public class AutorServicio {

    private final AutorDao autorDao;
    private final LibroDao libroDao;

    public AutorServicio(AutorDao autorDao, LibroDao libroDao) {
        this.autorDao = autorDao;
        this.libroDao = libroDao;
    }

    public List<Autor> listar() {
        return autorDao.buscarTodos();
    }

    public Autor obtener(int id) {
        return autorDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el autor " + id));
    }

    public List<Libro> librosDelAutor(int id) {
        obtener(id);   // 404 si el autor no existe
        return libroDao.buscar(null, null, id);
    }

    @Transactional
    public Autor crear(AutorPeticion datos) {
        int id = autorDao.insertar(datos);
        return obtener(id);
    }

    @Transactional
    public Autor actualizar(int id, AutorPeticion datos) {
        obtener(id);
        autorDao.actualizar(id, datos);
        return obtener(id);
    }

    @Transactional
    public void borrar(int id) {
        obtener(id);
        if (autorDao.contarLibros(id) > 0) {
            throw new ConflictoException("No se puede borrar el autor " + id + ": tiene libros");
        }
        autorDao.borrar(id);
    }
}
