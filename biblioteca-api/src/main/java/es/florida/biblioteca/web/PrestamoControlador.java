package es.florida.biblioteca.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.florida.biblioteca.modelo.Prestamo;
import es.florida.biblioteca.modelo.PrestamoPeticion;
import es.florida.biblioteca.servicio.PrestamoServicio;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoControlador {

    private final PrestamoServicio servicio;

    public PrestamoControlador(PrestamoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Prestamo> listar(@RequestParam(name = "estado", required = false) String estado) {
        return servicio.listar(estado);
    }

    @GetMapping("/{id}")
    public Prestamo obtener(@PathVariable("id") int id) {
        return servicio.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Prestamo> crear(@Valid @RequestBody PrestamoPeticion datos) {
        Prestamo creado = servicio.crear(datos);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}/devolucion")
    public Prestamo devolver(@PathVariable("id") int id) {
        return servicio.devolver(id);
    }
}
