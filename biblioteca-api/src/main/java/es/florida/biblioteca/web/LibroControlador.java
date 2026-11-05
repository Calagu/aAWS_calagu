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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.modelo.LibroPeticion;
import es.florida.biblioteca.servicio.LibroServicio;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/libros")
public class LibroControlador {

    private final LibroServicio servicio;

    public LibroControlador(LibroServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Libro> buscar(
            @RequestParam(name = "titulo", required = false) String titulo,
            @RequestParam(name = "genero", required = false) String genero,
            @RequestParam(name = "autorId", required = false) Integer autorId) {
        return servicio.buscar(titulo, genero, autorId);
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable("id") int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@Valid @RequestBody LibroPeticion datos) {
        Libro creado = servicio.crear(datos);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable("id") int id, @Valid @RequestBody LibroPeticion datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable("id") int id) {
        servicio.borrar(id);
    }
}
