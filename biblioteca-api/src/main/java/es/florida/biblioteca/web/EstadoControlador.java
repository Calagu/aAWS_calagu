package es.florida.biblioteca.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.florida.biblioteca.modelo.Estado;
import es.florida.biblioteca.servicio.EstadoServicio;

@RestController
@RequestMapping("/api/estado")
public class EstadoControlador {

    private final EstadoServicio servicio;

    public EstadoControlador(EstadoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Estado estado() {
        return servicio.consultar();
    }
}
