package es.florida.biblioteca.modelo;

import java.time.LocalDate;

/** Prestamo con su estado calculado: ACTIVO, VENCIDO o DEVUELTO. */
public record Prestamo(int id, int libroId, int socioId, LocalDate fechaPrestamo,
        LocalDate fechaLimite, LocalDate fechaDevolucion, String estado) {

    public static final String ACTIVO = "ACTIVO";
    public static final String VENCIDO = "VENCIDO";
    public static final String DEVUELTO = "DEVUELTO";

    /** Devuelto si tiene fecha de devolucion; vencido si no y su limite es anterior a hoy. */
    public static String calcularEstado(LocalDate fechaLimite, LocalDate fechaDevolucion, LocalDate hoy) {
        if (fechaDevolucion != null) {
            return DEVUELTO;
        }
        return fechaLimite.isBefore(hoy) ? VENCIDO : ACTIVO;
    }
}
