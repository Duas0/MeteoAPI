package es.mdai.meteoapi.service;

import es.mdai.meteoapi.model.EstacionMeteorologica;
import es.mdai.meteoapi.model.PuntoCarretera;
import es.mdai.meteoapi.model.ResultadoCercania;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class EstacionService {
    private final List<EstacionMeteorologica> estaciones = new CopyOnWriteArrayList<>();
    private final AtomicLong siguienteId = new AtomicLong(9);
    private final String nombreAplicacion;
    private final String descripcion;
    private final int minimoRayos;
    private final List<PuntoCarretera> puntosCarretera = List.of(
            new PuntoCarretera("A-6", 10.5, 40.4700, -3.7830),
            new PuntoCarretera("A-5", 340.0, 38.9230, -6.3310),
            new PuntoCarretera("A-66", 550.0, 39.4860, -6.4000),
            new PuntoCarretera("A-2", 308.0, 41.6500, -0.9000));

    public EstacionService(@Value("${meteoapi.nombre:MeteoAPI}") String nombreAplicacion,
            @Value("${meteoapi.descripcion:La meteorologia en tu mano}") String descripcion,
            @Value("${meteoapi.minimo-rayos:1}") int minimoRayos) {
        if (minimoRayos < 1) {
            throw new IllegalArgumentException("meteoapi.minimo-rayos debe ser un entero positivo.");
        }
        this.nombreAplicacion = nombreAplicacion;
        this.descripcion = descripcion;
        this.minimoRayos = minimoRayos;
        // Una fecha fija hace que las consultas de ejemplo sean reproducibles.
        Instant fecha = Instant.parse("2026-10-13T10:00:00Z");
        estaciones.add(new EstacionMeteorologica(1L, "Madrid Centro", "Madrid", "Madrid",
                40.4168, -3.7038, fecha, 21.5, 62, 14.4, 270, 0.4, 3));
        estaciones.add(new EstacionMeteorologica(2L, "Cáceres Centro", "Cáceres", "Cáceres",
                39.4753, -6.3722, fecha, 23.1, 52, 36.0, 210, 2.8, 7));
        estaciones.add(new EstacionMeteorologica(3L, "Badajoz Ribera", "Badajoz", "Badajoz",
                38.8794, -6.9707, fecha, 25.2, 48, 7.2, 180, 0.0, 0));
        estaciones.add(new EstacionMeteorologica(4L, "Mérida Río", "Mérida", "Badajoz",
                38.9161, -6.3437, fecha, 24.0, 55, 32.4, 225, 1.8, 1));
        estaciones.add(new EstacionMeteorologica(5L, "Salamanca Norte", "Salamanca", "Salamanca",
                40.9701, -5.6635, fecha, 16.3, 72, 43.2, 315, 4.1, 0));
        estaciones.add(new EstacionMeteorologica(6L, "Sevilla Parque", "Sevilla", "Sevilla",
                37.3891, -5.9845, fecha, 27.8, 45, 10.8, 90, 0.0, 0));
        estaciones.add(new EstacionMeteorologica(7L, "Valencia Puerto", "Valencia", "Valencia",
                39.4699, -0.3763, fecha, 24.7, 81, 28.8, 90, 6.2, 5));
        estaciones.add(new EstacionMeteorologica(8L, "Zaragoza Centro", "Zaragoza", "Zaragoza",
                41.6488, -0.8891, fecha, 18.5, 60, 54.0, 300, 0.2, 0));
    }

    public List<EstacionMeteorologica> listar(String nombre, String provincia, String municipio) {
        String texto = nombre == null ? "" : nombre.strip().toLowerCase(Locale.ROOT);
        return estaciones.stream()
                .filter(e -> e.getNombre().toLowerCase(Locale.ROOT).contains(texto))
                .filter(e -> coincide(e.getProvincia(), provincia))
                .filter(e -> coincide(e.getMunicipio(), municipio))
                .toList();
    }

    private boolean coincide(String valor, String filtro) {
        return filtro == null || filtro.isBlank() || valor.equalsIgnoreCase(filtro.strip());
    }

    public Optional<EstacionMeteorologica> buscarPorId(Long id) {
        return estaciones.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public List<EstacionMeteorologica> buscarConRayos() {
        return estaciones.stream()
                .filter(e -> e.getRayosUltimaHora() != null && e.getRayosUltimaHora() >= minimoRayos)
                .toList();
    }

    public Set<Long> idsConRayos() {
        return buscarConRayos().stream().map(EstacionMeteorologica::getId).collect(Collectors.toSet());
    }

    public List<EstacionMeteorologica> buscarPorViento(double minimo) {
        if (!Double.isFinite(minimo) || minimo < 0) {
            throw new IllegalArgumentException("El mínimo de viento debe ser un número finito no negativo.");
        }
        return estaciones.stream().filter(e -> e.getVientoKmh() != null && e.getVientoKmh() >= minimo).toList();
    }

    public List<String> listarProvincias() {
        return estaciones.stream().map(EstacionMeteorologica::getProvincia).distinct().sorted().toList();
    }

    public ResultadoCercania buscarCercana(Double latitud, Double longitud, String carretera, Double km) {
        boolean porCoordenadas = latitud != null || longitud != null;
        boolean porCarretera = carretera != null || km != null;
        if (porCoordenadas == porCarretera) {
            throw new IllegalArgumentException("Indica coordenadas o carretera y kilómetro, sin mezclar ambos.");
        }
        PuntoCarretera punto;
        if (porCoordenadas) {
            if (!enRango(latitud, -90, 90) || !enRango(longitud, -180, 180)) {
                throw new IllegalArgumentException("Indica latitud entre -90 y 90 y longitud entre -180 y 180.");
            }
            punto = new PuntoCarretera(null, 0, latitud, longitud);
        } else {
            if (carretera == null || carretera.isBlank() || !noNegativo(km) || km == null) {
                throw new IllegalArgumentException("Indica una carretera y un kilómetro finito no negativo.");
            }
            punto = puntosCarretera.stream()
                    .filter(p -> p.getCarretera().equalsIgnoreCase(carretera.strip()) && Double.compare(p.getKm(), km) == 0)
                    .findFirst().orElseThrow(() -> new NoSuchElementException("El punto kilométrico no está en el catálogo de ejemplo."));
        }
        EstacionMeteorologica cercana = estaciones.stream()
                .min(Comparator.<EstacionMeteorologica>comparingDouble(e -> distanciaKm(punto, e))
                        .thenComparing(EstacionMeteorologica::getId))
                .orElseThrow(() -> new NoSuchElementException("No hay estaciones disponibles."));
        return new ResultadoCercania(cercana, Math.round(distanciaKm(punto, cercana) * 100.0) / 100.0);
    }

    private double distanciaKm(PuntoCarretera punto, EstacionMeteorologica estacion) {
        // Distancia geográfica aproximada: no es distancia de conducción por carretera.
        double diferenciaLatitud = Math.toRadians(estacion.getLatitud() - punto.getLatitud());
        double diferenciaLongitud = Math.toRadians(estacion.getLongitud() - punto.getLongitud());
        double a = Math.pow(Math.sin(diferenciaLatitud / 2), 2)
                + Math.cos(Math.toRadians(punto.getLatitud())) * Math.cos(Math.toRadians(estacion.getLatitud()))
                * Math.pow(Math.sin(diferenciaLongitud / 2), 2);
        return 6371.0 * 2 * Math.asin(Math.sqrt(Math.min(1.0, a)));
    }

    public Map<String, String> validar(EstacionMeteorologica estacion) {
        Map<String, String> errores = new LinkedHashMap<>();
        validarTexto(errores, "nombre", estacion.getNombre(), 80);
        validarTexto(errores, "municipio", estacion.getMunicipio(), 80);
        validarTexto(errores, "provincia", estacion.getProvincia(), 60);
        if (!enRango(estacion.getLatitud(), -90, 90)) errores.put("latitud", "La latitud debe estar entre -90 y 90.");
        if (!enRango(estacion.getLongitud(), -180, 180)) errores.put("longitud", "La longitud debe estar entre -180 y 180.");
        if (estacion.getTemperaturaC() != null && !Double.isFinite(estacion.getTemperaturaC())) {
            errores.put("temperaturaC", "La temperatura debe ser un número finito.");
        }
        if (estacion.getHumedadPct() != null && (estacion.getHumedadPct() < 0 || estacion.getHumedadPct() > 100)) {
            errores.put("humedadPct", "La humedad debe estar entre 0 y 100.");
        }
        if (!noNegativo(estacion.getVientoKmh())) errores.put("vientoKmh", "El viento no puede ser negativo ni infinito.");
        if (estacion.getDireccionVientoGrados() != null
                && (estacion.getDireccionVientoGrados() < 0 || estacion.getDireccionVientoGrados() >= 360)) {
            errores.put("direccionVientoGrados", "La dirección debe estar entre 0 y 359 grados.");
        }
        if (!noNegativo(estacion.getPrecipitacionUltimaHoraMm())) {
            errores.put("precipitacionUltimaHoraMm", "La precipitación no puede ser negativa ni infinita.");
        }
        if (estacion.getRayosUltimaHora() != null && estacion.getRayosUltimaHora() < 0) {
            errores.put("rayosUltimaHora", "El número de rayos no puede ser negativo.");
        }
        return errores;
    }

    private void validarTexto(Map<String, String> errores, String campo, String valor, int maximo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > maximo) {
            errores.put(campo, "Campo obligatorio, con un máximo de " + maximo + " caracteres.");
        }
    }

    private boolean enRango(Double valor, double minimo, double maximo) {
        return valor != null && Double.isFinite(valor) && valor >= minimo && valor <= maximo;
    }

    private boolean noNegativo(Double valor) {
        return valor == null || (Double.isFinite(valor) && valor >= 0);
    }

    public EstacionMeteorologica anadir(EstacionMeteorologica estacion) {
        Map<String, String> errores = validar(estacion);
        if (!errores.isEmpty()) throw new IllegalArgumentException(String.join(" ", errores.values()));
        // Ni el id ni la fecha se aceptan del formulario: los asigna el servicio.
        estacion.setId(siguienteId.getAndIncrement());
        estacion.setFechaObservacion(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        estacion.setNombre(estacion.getNombre().strip());
        estacion.setMunicipio(estacion.getMunicipio().strip());
        estacion.setProvincia(estacion.getProvincia().strip());
        estaciones.add(estacion);
        return estacion;
    }

    public String getNombreAplicacion() { return nombreAplicacion; }
    public String getDescripcion() { return descripcion; }
    public int getMinimoRayos() { return minimoRayos; }
    public List<PuntoCarretera> getPuntosCarretera() { return puntosCarretera; }
}
