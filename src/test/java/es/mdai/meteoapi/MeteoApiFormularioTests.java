package es.mdai.meteoapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Un contexto separado evita que el alta cambie el catálogo de las otras pruebas.
@SpringBootTest(properties = "meteoapi.nombre=MeteoAPI formulario")
@AutoConfigureMockMvc
class MeteoApiFormularioTests {
    @Autowired
    private MockMvc navegador;

    @Test
    void validaEnServidorYConservaElFormulario() throws Exception {
        navegador.perform(post("/estaciones/nueva").param("nombre", "Estación inválida")
                        .param("municipio", "Madrid").param("provincia", "Madrid")
                        .param("latitud", "91").param("longitud", "-3")
                        .param("vientoKmh", "-1").param("humedadPct", "101"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("estacion", "latitud", "vientoKmh", "humedadPct"))
                .andExpect(content().string(containsString("Revisa los datos")));
        navegador.perform(post("/estaciones/nueva").param("nombre", "Prueba no finita")
                        .param("municipio", "Madrid").param("provincia", "Madrid")
                        .param("latitud", "40").param("longitud", "-3").param("temperaturaC", "NaN"))
                .andExpect(model().attributeHasFieldErrors("estacion", "temperaturaC"));
        navegador.perform(get("/api/estaciones").param("nombre", "inválida"))
                .andExpect(content().json("[]"));
    }

    @Test
    void altaRedirigeIgnoraCamposInternosYAdmiteMedidasAusentes() throws Exception {
        navegador.perform(post("/estaciones/nueva").param("nombre", "Estación de prueba")
                        .param("municipio", "Madrid").param("provincia", "Madrid")
                        .param("latitud", "40.4").param("longitud", "-3.7").param("vientoKmh", "18")
                        .param("id", "800").param("fechaObservacion", "1970-01-01T00:00:00Z"))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/estaciones"))
                .andExpect(flash().attribute("mensaje", containsString("id 9")));
        navegador.perform(get("/api/estaciones/9")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Estación de prueba"))
                .andExpect(jsonPath("$.vientoMs").value(5.0))
                .andExpect(jsonPath("$.humedadPct").isEmpty())
                .andExpect(jsonPath("$.fechaObservacion").value(not("1970-01-01T00:00:00Z")));
        navegador.perform(get("/estaciones/9")).andExpect(status().isOk())
                .andExpect(content().string(containsString("5,00 m/s")));
        navegador.perform(get("/estaciones")).andExpect(status().isOk());
        navegador.perform(get("/estaciones")).andExpect(status().isOk());
        navegador.perform(get("/api/estaciones").param("nombre", "Estación de prueba"))
                .andExpect(jsonPath("$.length()").value(1));
    }
}
