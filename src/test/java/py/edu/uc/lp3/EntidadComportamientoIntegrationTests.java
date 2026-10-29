package py.edu.uc.lp3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class EntidadComportamientoIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void inicioYTodasLasEntidadesTienenRutasDistintas() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bienvenido")));
        mvc.perform(get("/entidades")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void construyePorTipoYRechazaTiposDesconocidos() throws Exception {
        mvc.perform(get("/entidad/zombie")).andExpect(status().isOk())
                .andExpect(jsonPath("$.entidad.vida").value(20));
        mvc.perform(get("/entidad/desconocida")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/entidades/zombie", "/zombie"})
    void construyeZombieConParametros(String ruta) throws Exception {
        mvc.perform(get(ruta).param("vida", "30")).andExpect(status().isOk())
                .andExpect(jsonPath("$.entidad.vida").value(30));
        mvc.perform(get(ruta).param("vida", "0")).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/entidades/esqueleto/disparar", "/esqueleto/disparar"})
    void conservaSobrecargaDelEsqueleto(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk())
                .andExpect(jsonPath("$.disparar").isString());
        mvc.perform(get(ruta).param("distancia", "10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.disparar").isString());
        mvc.perform(get(ruta).param("distancia", "-1")).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/entidades/comportamientos", "/comportamientos"})
    void conservaComportamientosPolimorficos(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("Zombie"))
                .andExpect(jsonPath("$[1].tipo").value("Aldeano"));
    }
}
