package es.mdai.meteoapi.model;

/** La distancia depende de la consulta, por eso no pertenece a la estación. */
public class ResultadoCercania {
    private final EstacionMeteorologica estacion;
    private final double distanciaKm;

    public ResultadoCercania(EstacionMeteorologica estacion, double distanciaKm) {
        this.estacion = estacion;
        this.distanciaKm = distanciaKm;
    }

    public EstacionMeteorologica getEstacion() { return estacion; }
    public double getDistanciaKm() { return distanciaKm; }
}
