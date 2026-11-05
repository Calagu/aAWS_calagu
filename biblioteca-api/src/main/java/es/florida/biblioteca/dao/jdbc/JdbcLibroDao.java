package es.florida.biblioteca.dao.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import es.florida.biblioteca.dao.LibroDao;
import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;

@Repository
public class JdbcLibroDao implements LibroDao {

    /** Los ejemplares disponibles no se almacenan: ejemplares - prestamos abiertos. */
    private static final String SELECT_BASE =
            "SELECT l.id, l.isbn, l.titulo, l.anio_publicacion, l.genero, l.ejemplares, l.autor_id, "
            + "a.nombre AS autor_nombre, "
            + "l.ejemplares - (SELECT COUNT(*) FROM prestamo p "
            + "WHERE p.libro_id = l.id AND p.fecha_devolucion IS NULL) AS disponibles "
            + "FROM libro l JOIN autor a ON a.id = l.autor_id";

    private static final RowMapper<Libro> MAPEADOR = (rs, numFila) -> new Libro(
            rs.getInt("id"),
            rs.getString("isbn"),
            rs.getString("titulo"),
            rs.getObject("anio_publicacion", Integer.class),
            rs.getString("genero"),
            rs.getInt("ejemplares"),
            rs.getInt("autor_id"),
            rs.getString("autor_nombre"),
            rs.getInt("disponibles"));

    private final JdbcClient jdbc;

    public JdbcLibroDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Libro> buscar(String titulo, String genero, Integer autorId) {
        // Solo se concatenan fragmentos de SQL constantes; los valores van siempre como parametros.
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1");
        Map<String, Object> parametros = new HashMap<>();
        if (titulo != null) {
            sql.append(" AND LOWER(l.titulo) LIKE LOWER(:titulo)");
            parametros.put("titulo", "%" + escaparComodines(titulo) + "%");
        }
        if (genero != null) {
            sql.append(" AND LOWER(l.genero) = LOWER(:genero)");
            parametros.put("genero", genero);
        }
        if (autorId != null) {
            sql.append(" AND l.autor_id = :autorId");
            parametros.put("autorId", autorId);
        }
        sql.append(" ORDER BY l.titulo, l.id");
        return jdbc.sql(sql.toString())
                .params(parametros)
                .query(MAPEADOR)
                .list();
    }

    @Override
    public Optional<Libro> buscarPorId(int id) {
        return jdbc.sql(SELECT_BASE + " WHERE l.id = :id")
                .param("id", id)
                .query(MAPEADOR)
                .optional();
    }

    @Override
    public Optional<Integer> bloquearEjemplares(int id) {
        // Consulta simple (sin JOIN) para bloquear solo la fila del libro.
        return jdbc.sql("SELECT ejemplares FROM libro WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(Integer.class)
                .optional();
    }

    @Override
    public int insertar(LibroPeticion datos) {
        KeyHolder clave = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO libro (isbn, titulo, anio_publicacion, genero, ejemplares, autor_id) "
                + "VALUES (:isbn, :titulo, :anio, :genero, :ejemplares, :autorId)")
                .param("isbn", datos.isbn())
                .param("titulo", datos.titulo())
                .param("anio", datos.anioPublicacion())
                .param("genero", datos.genero())
                .param("ejemplares", datos.ejemplares())
                .param("autorId", datos.autorId())
                .update(clave, "id");
        return clave.getKey().intValue();
    }

    @Override
    public int actualizar(int id, LibroPeticion datos) {
        return jdbc.sql("UPDATE libro SET isbn = :isbn, titulo = :titulo, anio_publicacion = :anio, "
                + "genero = :genero, ejemplares = :ejemplares, autor_id = :autorId WHERE id = :id")
                .param("isbn", datos.isbn())
                .param("titulo", datos.titulo())
                .param("anio", datos.anioPublicacion())
                .param("genero", datos.genero())
                .param("ejemplares", datos.ejemplares())
                .param("autorId", datos.autorId())
                .param("id", id)
                .update();
    }

    @Override
    public int borrar(int id) {
        return jdbc.sql("DELETE FROM libro WHERE id = :id")
                .param("id", id)
                .update();
    }

    @Override
    public boolean existeIsbn(String isbn, Integer excluirId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM libro WHERE isbn = :isbn");
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("isbn", isbn);
        if (excluirId != null) {
            sql.append(" AND id <> :excluirId");
            parametros.put("excluirId", excluirId);
        }
        Integer total = jdbc.sql(sql.toString()).params(parametros).query(Integer.class).single();
        return total > 0;
    }

    @Override
    public int contarPrestamosAbiertos(int libroId) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE libro_id = :libroId "
                + "AND fecha_devolucion IS NULL")
                .param("libroId", libroId)
                .query(Integer.class)
                .single();
    }

    @Override
    public int contarPrestamos(int libroId) {
        return jdbc.sql("SELECT COUNT(*) FROM prestamo WHERE libro_id = :libroId")
                .param("libroId", libroId)
                .query(Integer.class)
                .single();
    }

    /** Escapa \, % y _ para que el texto del usuario se busque literalmente en un LIKE. */
    private static String escaparComodines(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
