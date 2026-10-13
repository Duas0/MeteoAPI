package es.mdai.meteoapi.model;

/** Referencia ficticia de un punto kilométrico; no sustituye a un servicio de mapas. */
public class PuntoCarretera {
    private final String carretera;
    private final double km;
    private final double latitud;
    private final double longitud;

    public PuntoCarretera(String carretera, double km, double latitud, double longitud) {
        this.carretera = carretera;
        this.km = km;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getCarretera() { return carretera; }
    public double getKm() { return km; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
}
