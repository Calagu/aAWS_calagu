package es.florida.biblioteca.modelo;

/** Respuesta de GET /api/estado. */
public record Estado(String estado, String alumno, Parametros parametros, String versionMysql,
        MemoriaJvm memoriaJvm) {

    public record Parametros(int prestamoMaximo, int prestamoDias) {
    }

    public record MemoriaJvm(long maximaMiB, long reservadaMiB, long usadaMiB) {
    }
}
