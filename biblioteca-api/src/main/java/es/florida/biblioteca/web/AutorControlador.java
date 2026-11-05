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

import es.florida.biblioteca.modelo.Autor;
import es.florida.biblioteca.modelo.AutorPeticion;
import es.florida.biblioteca.modelo.Libro;
import es.florida.biblioteca.servicio.AutorServicio;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/autores")
public class AutorControlador {

    private final AutorServicio servicio;

    public AutorControlador(AutorServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Autor> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Autor obtener(@PathVariable("id") int id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/libros")
    public List<Libro> libros(@PathVariable("id") int id) {
        return servicio.librosDelAutor(id);
    }

    @PostMapping
    public ResponseEntity<Autor> crear(@Valid @RequestBody AutorPeticion datos) {
        Autor creado = servicio.crear(datos);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public Autor actualizar(@PathVariable("id") int id, @Valid @RequestBody AutorPeticion datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable("id") int id) {
        servicio.borrar(id);
    }
}
