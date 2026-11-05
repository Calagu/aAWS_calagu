package es.florida.biblioteca.dao.jdbc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import es.florida.biblioteca.dao.PrestamoDao;
import es.florida.biblioteca.modelo.Prestamo;

@Repository
public class JdbcPrestamoDao implements PrestamoDao {

    private static final String SELECT_BASE =
            "SELECT id, libro_id, socio_id, fecha_prestamo, fecha_limite, fecha_devolucion FROM prestamo";

    private final JdbcClient jdbc;

    public JdbcPrestamoDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** El estado se calcula con la fecha de hoy, que se recibe como parametro. */
    private static RowMapper<Prestamo> mapeador(LocalDate hoy) {
        return (rs, numFila) -> {
            LocalDate limite = rs.getObject("fecha_limite", LocalDate.class);
            LocalDate devolucion = rs.getObject("fecha_devolucion", LocalDate.class);   // admite NULL
            return new Prestamo(
                    rs.getInt("id"),
                    rs.getInt("libro_id"),
                    rs.getInt("socio_id"),
                    rs.getObject("fecha_prestamo", LocalDate.class),
                    limite,
                    devolucion,
                    Prestamo.calcularEstado(limite, devolucion, hoy));
        };
    }

    @Override
    public List<Prestamo> buscar(String estado, LocalDate hoy) {
        // Fragmentos constantes elegidos segun el estado; el valor "hoy" va como parametro.
        String filtro = switch (estado == null ? "" : estado) {
            case Prestamo.ACTIVO -> " WHERE fecha_devolucion IS NULL AND fecha_limite >= :hoy";
            case Prestamo.VENCIDO -> " WHERE fecha_devolucion IS NULL AND fecha_limite < :hoy";
            case Prestamo.DEVUELTO -> " WHERE fecha_devolucion IS NOT NULL";
            default -> "";
        };
        var consulta = jdbc.sql(SELECT_BASE + filtro + " ORDER BY id");
        if (filtro.contains(":hoy")) {
            consulta = consulta.param("hoy", hoy);
        }
        return consulta.query(mapeador(hoy)).list();
    }

    @Override
    public List<Prestamo> buscarPorSocio(int socioId, LocalDate hoy) {
        return jdbc.sql(SELECT_BASE + " WHERE socio_id = :socioId ORDER BY fecha_prestamo DESC, id DESC")
                .param("socioId", socioId)
                .query(mapeador(hoy))
                .list();
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id, LocalDate hoy) {
        return jdbc.sql(SELECT_BASE + " WHERE id = :id")
                .param("id", id)
                .query(mapeador(hoy))
                .optional();
    }

    @Override
    public Optional<Prestamo> bloquearPorId(int id, LocalDate hoy) {
        return jdbc.sql(SELECT_BASE + " WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(mapeador(hoy))
                .optional();
    }

    @Override
    public int insertar(int libroId, int socioId, LocalDate fechaPrestamo, LocalDate fechaLimite) {
        KeyHolder clave = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO prestamo (libro_id, socio_id, fecha_prestamo, fecha_limite) "
                + "VALUES (:libroId, :socioId, :fechaPrestamo, :fechaLimite)")
                .param("libroId", libroId)
                .param("socioId", socioId)
                .param("fechaPrestamo", fechaPrestamo)
                .param("fechaLimite", fechaLimite)
                .update(clave, "id");
        return clave.getKey().intValue();
    }

    @Override
    public int registrarDevolucion(int id, LocalDate fecha) {
        return jdbc.sql("UPDATE prestamo SET fecha_devolucion = :fecha WHERE id = :id")
                .param("fecha", fecha)
                .param("id", id)
                .update();
    }

    @Override
    public int contarVencidosDeSocio(int socioId, LocalDate hoy) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE socio_id = :socioId "
                + "AND fecha_devolucion IS NULL AND fecha_limite < :hoy")
                .param("socioId", socioId)
                .param("hoy", hoy)
                .query(Integer.class)
                .single();
    }

    @Override
    public int contarAbiertosDeSocio(int socioId) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE socio_id = :socioId "
                + "AND fecha_devolucion IS NULL")
                .param("socioId", socioId)
                .query(Integer.class)
                .single();
    }

    @Override
    public boolean tieneAbiertoDelLibro(int socioId, int libroId) {
        Integer total = jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE socio_id = :socioId "
                + "AND libro_id = :libroId AND fecha_devolucion IS NULL")
                .param("socioId", socioId)
                .param("libroId", libroId)
                .query(Integer.class)
                .single();
        return total > 0;
    }

    @Override
    public int contarAbiertosDeLibro(int libroId) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE libro_id = :libroId "
                + "AND fecha_devolucion IS NULL")
                .param("libroId", libroId)
                .query(Integer.class)
                .single();
    }
}
