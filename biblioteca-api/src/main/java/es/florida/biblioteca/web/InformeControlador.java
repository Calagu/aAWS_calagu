package es.florida.biblioteca.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.florida.biblioteca.modelo.LibroMasPrestado;
import es.florida.biblioteca.servicio.InformeServicio;

/** Informe de la variante 1: GET /api/informes/mas-prestados?limite=n (1-20, 5 por defecto). */
@RestController
@RequestMapping("/api/informes")
public class InformeControlador {

    private final InformeServicio servicio;

    public InformeControlador(InformeServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/mas-prestados")
    public List<LibroMasPrestado> masPrestados(
            @RequestParam(name = "limite", defaultValue = "5") int limite) {
        return servicio.masPrestados(limite);
    }
}
