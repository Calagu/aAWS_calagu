package es.florida.biblioteca.servicio;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.florida.biblioteca.dao.PrestamoDao;
import es.florida.biblioteca.dao.SocioDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.Socio;
import es.florida.biblioteca.modelo.SocioPeticion;

@Service
public class SocioServicio {

    private final SocioDao socioDao;
    private final PrestamoDao prestamoDao;
    private final Clock reloj;

    public SocioServicio(SocioDao socioDao, PrestamoDao prestamoDao, Clock reloj) {
        this.socioDao = socioDao;
        this.prestamoDao = prestamoDao;
        this.reloj = reloj;
    }

    public List<Socio> listar() {
        return socioDao.buscarTodos();
    }

    public Socio obtener(int id) {
        return socioDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el socio " + id));
    }

    public List<Prestamo> historial(int id) {
        obtener(id);   // 404 si el socio no existe
        return prestamoDao.buscarPorSocio(id, LocalDate.now(reloj));
    }

    @Transactional
    public Socio crear(SocioPeticion datos) {
        if (socioDao.existeEmail(datos.email(), null)) {
            throw new ConflictoException("Ya existe un socio con el email " + datos.email());
        }
        int id = socioDao.insertar(datos, LocalDate.now(reloj));   // la fecha de alta la asigna el servidor
        return obtener(id);
    }

    @Transactional
    public Socio actualizar(int id, SocioPeticion datos) {
        obtener(id);
        if (socioDao.existeEmail(datos.email(), id)) {
            throw new ConflictoException("Ya existe otro socio con el email " + datos.email());
        }
        socioDao.actualizar(id, datos);
        return obtener(id);
    }

    @Transactional
    public void borrar(int id) {
        obtener(id);
        if (socioDao.contarPrestamos(id) > 0) {
            throw new ConflictoException("No se puede borrar el socio " + id + ": tiene préstamos registrados");
        }
        socioDao.borrar(id);
    }
}
