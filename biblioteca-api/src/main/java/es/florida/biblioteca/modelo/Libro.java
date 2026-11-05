package es.florida.biblioteca.modelo;

/** Libro con el nombre del autor y los ejemplares disponibles (calculados, no almacenados). */
public record Libro(int id, String isbn, String titulo, Integer anioPublicacion, String genero,
        int ejemplares, int autorId, String autorNombre, int disponibles) {
}
