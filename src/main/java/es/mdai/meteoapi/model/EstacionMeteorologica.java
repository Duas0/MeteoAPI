package es.mdai.meteoapi.model;

import java.time.Instant;

public class EstacionMeteorologica {
    private Long id;
    private String nombre;
    private String municipio;
    private String provincia;
    private Double latitud;
    private Double longitud;
    private Instant fechaObservacion;
    private Double temperaturaC;
    private Integer humedadPct;
    private Double vientoKmh;
    private Integer direccionVientoGrados;
    private Double precipitacionUltimaHoraMm;
    private Integer rayosUltimaHora;

    public EstacionMeteorologica() {
    }

    public EstacionMeteorologica(Long id, String nombre, String municipio, String provincia,
            Double latitud, Double longitud, Instant fechaObservacion, Double temperaturaC,
            Integer humedadPct, Double vientoKmh, Integer direccionVientoGrados,
            Double precipitacionUltimaHoraMm, Integer rayosUltimaHora) {
        this.id = id;
        this.nombre = nombre;
        this.municipio = municipio;
        this.provincia = provincia;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaObservacion = fechaObservacion;
        this.temperaturaC = temperaturaC;
        this.humedadPct = humedadPct;
        this.vientoKmh = vientoKmh;
        this.direccionVientoGrados = direccionVientoGrados;
        this.precipitacionUltimaHoraMm = precipitacionUltimaHoraMm;
        this.rayosUltimaHora = rayosUltimaHora;
    }

    // Se deriva de la medida original: no guardamos dos velocidades que puedan discrepar.
    public Double getVientoMs() {
        return vientoKmh == null ? null : Math.round(vientoKmh / 3.6 * 100.0) / 100.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public Instant getFechaObservacion() { return fechaObservacion; }
    public void setFechaObservacion(Instant fechaObservacion) { this.fechaObservacion = fechaObservacion; }
    public Double getTemperaturaC() { return temperaturaC; }
    public void setTemperaturaC(Double temperaturaC) { this.temperaturaC = temperaturaC; }
    public Integer getHumedadPct() { return humedadPct; }
    public void setHumedadPct(Integer humedadPct) { this.humedadPct = humedadPct; }
    public Double getVientoKmh() { return vientoKmh; }
    public void setVientoKmh(Double vientoKmh) { this.vientoKmh = vientoKmh; }
    public Integer getDireccionVientoGrados() { return direccionVientoGrados; }
    public void setDireccionVientoGrados(Integer direccionVientoGrados) {
        this.direccionVientoGrados = direccionVientoGrados;
    }
    public Double getPrecipitacionUltimaHoraMm() { return precipitacionUltimaHoraMm; }
    public void setPrecipitacionUltimaHoraMm(Double precipitacionUltimaHoraMm) {
        this.precipitacionUltimaHoraMm = precipitacionUltimaHoraMm;
    }
    public Integer getRayosUltimaHora() { return rayosUltimaHora; }
    public void setRayosUltimaHora(Integer rayosUltimaHora) { this.rayosUltimaHora = rayosUltimaHora; }
}
