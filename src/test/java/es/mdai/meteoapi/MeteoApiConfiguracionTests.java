package es.mdai.meteoapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "meteoapi.minimo-rayos=5")
@AutoConfigureMockMvc
class MeteoApiConfiguracionTests {
    @Autowired
    private MockMvc navegador;

    @Test
    void elUmbralCambiaLaApiYElListado() throws Exception {
        navegador.perform(get("/api/estaciones/con-rayos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2)).andExpect(jsonPath("$[1].id").value(7));
        navegador.perform(get("/estaciones")).andExpect(status().isOk())
                .andExpect(content().string(containsString("5 o más rayos")));
    }
}
