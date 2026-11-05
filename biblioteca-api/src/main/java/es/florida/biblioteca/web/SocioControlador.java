package es.florida.biblioteca.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.Socio;
import es.florida.biblioteca.modelo.SocioPeticion;
import es.florida.biblioteca.servicio.SocioServicio;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/socios")
public class SocioControlador {

    private final SocioServicio servicio;

    public SocioControlador(SocioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Socio> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Socio obtener(@PathVariable("id") int id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/prestamos")
    public List<Prestamo> historial(@PathVariable("id") int id) {
        return servicio.historial(id);
    }

    @PostMapping
    public ResponseEntity<Socio> crear(@Valid @RequestBody SocioPeticion datos) {
        Socio creado = servicio.crear(datos);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public Socio actualizar(@PathVariable("id") int id, @Valid @RequestBody SocioPeticion datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable("id") int id) {
        servicio.borrar(id);
    }
}
