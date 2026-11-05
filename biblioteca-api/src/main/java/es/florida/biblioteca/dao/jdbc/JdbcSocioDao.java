package es.florida.biblioteca.dao.jdbc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import es.florida.biblioteca.dao.SocioDao;
import es.florida.biblioteca.modelo.Socio;
import es.florida.biblioteca.modelo.SocioPeticion;

@Repository
public class JdbcSocioDao implements SocioDao {

    private static final String SELECT_BASE =
            "SELECT s.id, s.nombre, s.email, s.fecha_alta, "
            + "(SELECT COUNT(*) FROM prestamo p WHERE p.socio_id = s.id "
            + "AND p.fecha_devolucion IS NULL) AS prestamos_abiertos "
            + "FROM socio s";

    private static final RowMapper<Socio> MAPEADOR = (rs, numFila) -> new Socio(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("email"),
            rs.getObject("fecha_alta", LocalDate.class),
            rs.getInt("prestamos_abiertos"));

    private final JdbcClient jdbc;

    public JdbcSocioDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Socio> buscarTodos() {
        return jdbc.sql(SELECT_BASE + " ORDER BY s.id")
                .query(MAPEADOR)
                .list();
    }

    @Override
    public Optional<Socio> buscarPorId(int id) {
        return jdbc.sql(SELECT_BASE + " WHERE s.id = :id")
                .param("id", id)
                .query(MAPEADOR)
                .optional();
    }

    @Override
    public boolean bloquearPorId(int id) {
        return jdbc.sql("SELECT id FROM socio WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    @Override
    public int insertar(SocioPeticion datos, LocalDate fechaAlta) {
        KeyHolder clave = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO socio (nombre, email, fecha_alta) VALUES (:nombre, :email, :fechaAlta)")
                .param("nombre", datos.nombre())
                .param("email", datos.email())
                .param("fechaAlta", fechaAlta)
                .update(clave, "id");
        return clave.getKey().intValue();
    }

    @Override
    public int actualizar(int id, SocioPeticion datos) {
        return jdbc.sql("UPDATE socio SET nombre = :nombre, email = :email WHERE id = :id")
                .param("nombre", datos.nombre())
                .param("email", datos.email())
                .param("id", id)
                .update();
    }

    @Override
    public int borrar(int id) {
        return jdbc.sql("DELETE FROM socio WHERE id = :id")
                .param("id", id)
                .update();
    }

    @Override
    public boolean existeEmail(String email, Integer excluirId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM socio WHERE LOWER(email) = LOWER(:email)");
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("email", email);
        if (excluirId != null) {
            sql.append(" AND id <> :excluirId");
            parametros.put("excluirId", excluirId);
        }
        Integer total = jdbc.sql(sql.toString()).params(parametros).query(Integer.class).single();
        return total > 0;
    }

    @Override
    public int contarPrestamos(int socioId) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE socio_id = :socioId")
                .param("socioId", socioId)
                .query(Integer.class)
                .single();
    }
}
