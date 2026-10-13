package es.mdai.meteoapi.controller;

import es.mdai.meteoapi.model.EstacionMeteorologica;
import es.mdai.meteoapi.service.EstacionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
public class EstacionController {
    private final EstacionService estacionService;

    public EstacionController(EstacionService estacionService) {
        this.estacionService = estacionService;
    }

    @ModelAttribute
    public void datosComunes(Model modelo) {
        modelo.addAttribute("nombreAplicacion", estacionService.getNombreAplicacion());
        modelo.addAttribute("descripcion", estacionService.getDescripcion());
        modelo.addAttribute("minimoRayos", estacionService.getMinimoRayos());
    }

    @GetMapping("/estaciones")
    public String listado(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) String provincia, @RequestParam(required = false) String municipio,
            Model modelo) {
        modelo.addAttribute("estaciones", estacionService.listar(nombre, provincia, municipio));
        modelo.addAttribute("provincias", estacionService.listarProvincias());
        modelo.addAttribute("idsConRayos", estacionService.idsConRayos());
        modelo.addAttribute("nombre", nombre);
        modelo.addAttribute("provincia", provincia == null || provincia.isBlank() ? null : provincia.strip());
        modelo.addAttribute("municipio", municipio);
        return "estaciones";
    }

    @GetMapping("/estaciones/{id}")
    public String detalle(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("estacion", estacionService.buscarPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una estación con id " + id)));
        modelo.addAttribute("idsConRayos", estacionService.idsConRayos());
        return "detalle";
    }

    @GetMapping("/ubicacion")
    public String ubicacion(@RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud, @RequestParam(required = false) String carretera,
            @RequestParam(required = false) Double km, Model modelo, HttpServletResponse respuesta) {
        modelo.addAttribute("puntosCarretera", estacionService.getPuntosCarretera());
        modelo.addAttribute("latitud", latitud);
        modelo.addAttribute("longitud", longitud);
        modelo.addAttribute("carretera", carretera);
        modelo.addAttribute("km", km);
        if (latitud != null || longitud != null || carretera != null || km != null) {
            try {
                modelo.addAttribute("resultado", estacionService.buscarCercana(latitud, longitud, carretera, km));
            } catch (IllegalArgumentException error) {
                respuesta.setStatus(HttpStatus.BAD_REQUEST.value());
                modelo.addAttribute("errorUbicacion", error.getMessage());
            } catch (NoSuchElementException error) {
                respuesta.setStatus(HttpStatus.NOT_FOUND.value());
                modelo.addAttribute("errorUbicacion", error.getMessage());
            }
        }
        return "ubicacion";
    }

    @GetMapping("/estaciones/nueva")
    public String formulario(Model modelo) {
        modelo.addAttribute("estacion", new EstacionMeteorologica());
        return "nueva-estacion";
    }

    @PostMapping("/estaciones/nueva")
    public String guardar(@ModelAttribute("estacion") EstacionMeteorologica estacion,
            BindingResult errores, RedirectAttributes redireccion) {
        estacionService.validar(estacion).forEach((campo, mensaje) -> {
            if (!errores.hasFieldErrors(campo)) errores.rejectValue(campo, "dato.invalido", mensaje);
        });
        if (errores.hasErrors()) return "nueva-estacion";
        EstacionMeteorologica guardada = estacionService.anadir(estacion);
        redireccion.addFlashAttribute("mensaje", guardada.getNombre() + " se ha añadido con el id " + guardada.getId() + ".");
        return "redirect:/estaciones";
    }

    @InitBinder("estacion")
    public void camposPermitidos(WebDataBinder binder) {
        binder.setAllowedFields("nombre", "municipio", "provincia", "latitud", "longitud", "temperaturaC",
                "humedadPct", "vientoKmh", "direccionVientoGrados", "precipitacionUltimaHoraMm", "rayosUltimaHora");
    }
}
