package es.florida.biblioteca.modelo;

import jakarta.validation.constraints.NotNull;

public record PrestamoPeticion(
        @NotNull(message = "el libroId es obligatorio") Integer libroId,
        @NotNull(message = "el socioId es obligatorio") Integer socioId) {
}
