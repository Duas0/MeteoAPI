package es.mdai.meteoapi.controller;

import es.mdai.meteoapi.model.EstacionMeteorologica;
import es.mdai.meteoapi.model.ResultadoCercania;
import es.mdai.meteoapi.service.EstacionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionRestController {
    private final EstacionService estacionService;

    public EstacionRestController(EstacionService estacionService) {
        this.estacionService = estacionService;
    }

    @GetMapping
    public List<EstacionMeteorologica> listar(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) String provincia, @RequestParam(required = false) String municipio) {
        return estacionService.listar(nombre, provincia, municipio);
    }

    @GetMapping("/{id}")
    public EstacionMeteorologica obtener(@PathVariable Long id) {
        return estacionService.buscarPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una estación con id " + id));
    }

    @GetMapping("/con-rayos")
    public List<EstacionMeteorologica> conRayos() {
        return estacionService.buscarConRayos();
    }

    @GetMapping("/viento")
    public List<EstacionMeteorologica> viento(@RequestParam(defaultValue = "30") double minimo) {
        try {
            return estacionService.buscarPorViento(minimo);
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error.getMessage());
        }
    }

    @GetMapping("/cercana")
    public ResultadoCercania cercana(@RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud, @RequestParam(required = false) String carretera,
            @RequestParam(required = false) Double km) {
        try {
            return estacionService.buscarCercana(latitud, longitud, carretera, km);
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error.getMessage());
        } catch (NoSuchElementException error) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, error.getMessage());
        }
    }
}
