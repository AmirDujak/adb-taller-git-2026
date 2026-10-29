package py.edu.uc.lp3;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.repository.EntidadRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class EntidadPosicionIntegrationTests {

    private static final String BASE = """
            "nombre":"Mi Creeper","vida":20,"danoBase":5,"velocidadMovimiento":0.7,
            "hitbox":{"ancho":0.6,"alto":1.7,"profundidad":0.6},"sonidos":[],"drop":[]
            """;
    private static final String EXTRAS = """
            ,"tipo":"CREEPER","cargado":true,"posicion":{"x":-1,"y":64,"z":2}
            """;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private EntidadRepository repository;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void creaYActualizaPosicionTipoYCargaSinCambiarElId() throws Exception {
        mvc.perform(post(ApiPaths.ENTIDADES).contentType(MediaType.APPLICATION_JSON)
                        .content("{" + BASE + EXTRAS + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CREEPER"))
                .andExpect(jsonPath("$.cargado").value(true))
                .andExpect(jsonPath("$.posicion.x").value(-1));
        Long id = repository.findAll().getFirst().getId();
        String ruta = ApiPaths.ENTIDADES + "/" + id;
        mvc.perform(put(ruta).contentType(MediaType.APPLICATION_JSON)
                        .content("{" + BASE + EXTRAS.replace("\"x\":-1", "\"x\":5") + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        // Los clientes anteriores que no envían los campos nuevos no los borran.
        mvc.perform(put(ruta).contentType(MediaType.APPLICATION_JSON).content("{" + BASE + "}"))
                .andExpect(status().isOk());
        mvc.perform(get(ruta)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CREEPER"))
                .andExpect(jsonPath("$.cargado").value(true))
                .andExpect(jsonPath("$.posicion.x").value(5));
    }

    @ParameterizedTest
    @MethodSource("extrasInvalidos")
    void rechazaPosicionesInvalidasYTiposIncompatibles(String extras) throws Exception {
        mvc.perform(post(ApiPaths.ENTIDADES).contentType(MediaType.APPLICATION_JSON)
                        .content("{" + BASE + extras + "}"))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    static Stream<String> extrasInvalidos() {
        return Stream.of(
                EXTRAS.replace("\"x\":-1", "\"x\":null"),
                EXTRAS.replace("\"y\":64", "\"y\":1e309"),
                EXTRAS.replace("\"z\":2", "\"z\":null"),
                EXTRAS.replace("\"CREEPER\"", "\"ZOMBIE\""),
                EXTRAS.replace("\"CREEPER\"", "\"DESCONOCIDO\""));
    }
}
