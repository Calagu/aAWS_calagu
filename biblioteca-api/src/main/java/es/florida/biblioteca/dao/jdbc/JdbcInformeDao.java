package es.florida.biblioteca.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import es.florida.biblioteca.dao.InformeDao;
import es.florida.biblioteca.modelo.LibroMasPrestado;

@Repository
public class JdbcInformeDao implements InformeDao {

    private static final RowMapper<LibroMasPrestado> MAPEADOR = (rs, numFila) -> new LibroMasPrestado(
            rs.getInt("id"),
            rs.getString("titulo"),
            rs.getLong("prestamos"));

    private final JdbcClient jdbc;

    public JdbcInformeDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Agregacion en SQL (no en Java): prestamos historicos por libro, de mayor a menor. */
    @Override
    public List<LibroMasPrestado> masPrestados(int limite) {
        return jdbc.sql("SELECT l.id, l.titulo, COUNT(p.id) AS prestamos "
                + "FROM libro l JOIN prestamo p ON p.libro_id = l.id "
                + "GROUP BY l.id, l.titulo "
                + "ORDER BY prestamos DESC, l.titulo ASC, l.id ASC "
                + "LIMIT :limite")
                .param("limite", limite)
                .query(MAPEADOR)
                .list();
    }
}
