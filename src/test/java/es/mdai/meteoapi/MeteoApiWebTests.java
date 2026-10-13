package es.mdai.meteoapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MeteoApiWebTests {
    @Autowired
    private MockMvc navegador;

    @Test
    void listadoFiltrosYValorCalculado() throws Exception {
        navegador.perform(get("/api/estaciones")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].vientoMs").value(4.0));
        navegador.perform(get("/api/estaciones").param("provincia", "badajoz"))
                .andExpect(jsonPath("$.length()").value(2));
        navegador.perform(get("/api/estaciones").param("municipio", "mAdRiD").param("nombre", "CENTRO"))
                .andExpect(jsonPath("$.length()").value(1));
        navegador.perform(get("/api/estaciones").param("provincia", "Madrid").param("municipio", "Sevilla"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        navegador.perform(get("/api/estaciones/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaObservacion").value("2026-10-13T10:00:00Z"));
    }

    @Test
    void consultasMeteorologicasYParametroPorDefecto() throws Exception {
        navegador.perform(get("/api/estaciones/con-rayos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        navegador.perform(get("/api/estaciones/viento")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        navegador.perform(get("/api/estaciones/viento").param("minimo", "40"))
                .andExpect(jsonPath("$.length()").value(2));
        navegador.perform(get("/api/estaciones/viento").param("minimo", "1000"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void erroresHttpYNumerosNoFinitos() throws Exception {
        navegador.perform(get("/api/estaciones/9999")).andExpect(status().isNotFound());
        navegador.perform(get("/api/estaciones/abc")).andExpect(status().isBadRequest());
        for (String invalido : new String[]{"abc", "-1", "NaN", "Infinity"}) {
            navegador.perform(get("/api/estaciones/viento").param("minimo", invalido))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void ubicacionPorCoordenadasYCarretera() throws Exception {
        navegador.perform(get("/api/estaciones/cercana").param("latitud", "40.4168").param("longitud", "-3.7038"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estacion.id").value(1))
                .andExpect(jsonPath("$.distanciaKm").value(0.0));
        navegador.perform(get("/api/estaciones/cercana").param("carretera", "a-5").param("km", "340"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estacion.id").value(4));
        navegador.perform(get("/api/estaciones/cercana").param("carretera", "A-6").param("km", "9999"))
                .andExpect(status().isNotFound());
        navegador.perform(get("/api/estaciones/cercana")).andExpect(status().isBadRequest());
        navegador.perform(get("/api/estaciones/cercana").param("latitud", "40"))
                .andExpect(status().isBadRequest());
        navegador.perform(get("/api/estaciones/cercana").param("latitud", "NaN").param("longitud", "0"))
                .andExpect(status().isBadRequest());
        navegador.perform(get("/api/estaciones/cercana").param("latitud", "91").param("longitud", "0"))
                .andExpect(status().isBadRequest());
        navegador.perform(get("/api/estaciones/cercana").param("latitud", "40").param("longitud", "-3")
                        .param("carretera", "A-6").param("km", "10.5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paginasRenderizadasFiltrosYDetalle() throws Exception {
        navegador.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
        navegador.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("La meteorología")));
        navegador.perform(get("/estaciones")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Madrid Centro")))
                .andExpect(content().string(not(containsString("th:text="))));
        navegador.perform(get("/estaciones").param("provincia", "Atlantis"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("No hay estaciones")));
        navegador.perform(get("/estaciones/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("4,00 m/s")));
        navegador.perform(get("/estaciones/9999")).andExpect(status().isNotFound());
        navegador.perform(get("/estaciones/abc")).andExpect(status().isBadRequest());
        navegador.perform(get("/ubicacion")).andExpect(status().isOk());
        navegador.perform(get("/ubicacion").param("latitud", "40.42").param("longitud", "-3.70"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/estaciones/cercana?latitud=40.42&amp;longitud=-3.7")))
                .andExpect(content().string(not(containsString("carretera=&amp;km="))));
        navegador.perform(get("/ubicacion").param("carretera", "A-5").param("km", "340"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Mérida Río")));
        navegador.perform(get("/ubicacion").param("carretera", "A-5").param("km", "9999"))
                .andExpect(status().isNotFound()).andExpect(content().string(containsString("catálogo de ejemplo")));
        navegador.perform(get("/estaciones/nueva")).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"temperaturaC\"")));
    }
}
