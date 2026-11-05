package es.florida.biblioteca.servicio;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.florida.biblioteca.config.BibliotecaProperties;
import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.dao.PrestamoDao;
import es.florida.biblioteca.dao.SocioDao;
import es.florida.biblioteca.excepcion.ConflictoException;
import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.excepcion.RecursoNoEncontradoException;
import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.PrestamoPeticion;

@Service
public class PrestamoServicio {

    private static final Set<String> ESTADOS = Set.of(Prestamo.ACTIVO, Prestamo.VENCIDO, Prestamo.DEVUELTO);

    private final PrestamoDao prestamoDao;
    private final LibroDao libroDao;
    private final SocioDao socioDao;
    private final BibliotecaProperties propiedades;
    private final Clock reloj;

    public PrestamoServicio(PrestamoDao prestamoDao, LibroDao libroDao, SocioDao socioDao,
            BibliotecaProperties propiedades, Clock reloj) {
        this.prestamoDao = prestamoDao;
        this.libroDao = libroDao;
        this.socioDao = socioDao;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    public List<Prestamo> listar(String estado) {
        String filtro = null;
        if (estado != null && !estado.isBlank()) {
            filtro = estado.trim().toUpperCase(Locale.ROOT);
            if (!ESTADOS.contains(filtro)) {
                throw new DatosInvalidosException("Estado no válido: " + estado
                        + ". Valores admitidos: ACTIVO, VENCIDO, DEVUELTO");
            }
        }
        return prestamoDao.buscar(filtro, LocalDate.now(reloj));
    }

    public Prestamo obtener(int id) {
        return prestamoDao.buscarPorId(id, LocalDate.now(reloj))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el préstamo " + id));
    }

    /**
     * Alta de un prestamo: una sola transaccion, cinco reglas en orden y filas bloqueadas.
     * IMPORTANTE: antes de los bloqueos no se hace ninguna lectura normal; asi, cuando una
     * transaccion espera el bloqueo, despues ve los datos ya confirmados por la otra.
     */
    @Transactional
    public Prestamo crear(PrestamoPeticion datos) {
        LocalDate hoy = LocalDate.now(reloj);
        int libroId = datos.libroId();
        int socioId = datos.socioId();
        int maximo = propiedades.prestamo().maximo();
        int dias = propiedades.prestamo().dias();

        // Regla 1: el libro y el socio existen (400). Bloqueo SIEMPRE en el mismo orden: libro, socio.
        Integer ejemplares = libroDao.bloquearEjemplares(libroId)          // SELECT ... FOR UPDATE (libro)
                .orElseThrow(() -> new DatosInvalidosException("El libro " + libroId + " no existe"));
        if (!socioDao.bloquearPorId(socioId)) {                            // SELECT ... FOR UPDATE (socio)
            throw new DatosInvalidosException("El socio " + socioId + " no existe");
        }

        // Regla 2: el socio no tiene ningun prestamo vencido (409).
        if (prestamoDao.contarVencidosDeSocio(socioId, hoy) > 0) {
            throw new ConflictoException("El socio " + socioId + " tiene préstamos vencidos");
        }

        // Regla 3: el socio no ha alcanzado el maximo de prestamos abiertos de la variante (409).
        if (prestamoDao.contarAbiertosDeSocio(socioId) >= maximo) {
            throw new ConflictoException("El socio " + socioId
                    + " ha alcanzado el máximo de " + maximo + " préstamos abiertos");
        }

        // Regla 4: el socio no tiene ya abierto un prestamo del mismo libro (409).
        if (prestamoDao.tieneAbiertoDelLibro(socioId, libroId)) {
            throw new ConflictoException("El socio " + socioId
                    + " ya tiene abierto un préstamo del libro " + libroId);
        }

        // Regla 5: queda al menos un ejemplar disponible (409).
        if (ejemplares - prestamoDao.contarAbiertosDeLibro(libroId) < 1) {
            throw new ConflictoException("No quedan ejemplares disponibles del libro " + libroId);
        }

        int id = prestamoDao.insertar(libroId, socioId, hoy, hoy.plusDays(dias));
        return obtener(id);
    }

    @Transactional
    public Prestamo devolver(int id) {
        LocalDate hoy = LocalDate.now(reloj);
        Prestamo prestamo = prestamoDao.bloquearPorId(id, hoy)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el préstamo " + id));
        if (prestamo.fechaDevolucion() != null) {
            throw new ConflictoException("El préstamo " + id + " ya fue devuelto el " + prestamo.fechaDevolucion());
        }
        prestamoDao.registrarDevolucion(id, hoy);
        return obtener(id);
    }
}
