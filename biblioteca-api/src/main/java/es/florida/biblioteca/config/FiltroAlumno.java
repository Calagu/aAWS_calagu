package es.florida.biblioteca.config;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Anade la cabecera X-Alumno a TODAS las respuestas, tambien a las de error. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroAlumno extends OncePerRequestFilter {

    public static final String CABECERA = "X-Alumno";

    private final BibliotecaProperties propiedades;

    public FiltroAlumno(BibliotecaProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {
        respuesta.setHeader(CABECERA, propiedades.alumno());
        cadena.doFilter(peticion, respuesta);
    }
}
