package py.edu.uc.lp3.adb.adb_taller_git_2026;

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
import py.edu.uc.lp3.adb.adb_taller_git_2026.repository.EntidadRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class EntidadCrudIntegrationTests {

    private static final String URL = "/api/entidades";
    private static final String DATOS = """
            {"nombre":"Zombie","vida":20,"danoBase":5,"velocidadMovimiento":0.7,
             "hitbox":{"ancho":0.6,"alto":1.95,"profundidad":0.6},
             "sonidos":["gruñido","gemido"],"drop":["carne_podrida"]}
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
    void crudCompletoPersisteEnH2() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Zombie"));

        Long id = repository.findAll().getFirst().getId();
        mvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vida").value(20))
                .andExpect(jsonPath("$.sonidos[1]").value("gemido"))
                .andExpect(jsonPath("$.hitbox.alto").value(1.95));
        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(DATOS.replace("\"vida\":20", "\"vida\":30")
                                .replace("[\"gruñido\",\"gemido\"]", "[\"pasos\"]")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.vida").value(30));
        mvc.perform(get(URL + "/" + id))
                .andExpect(jsonPath("$.vida").value(30))
                .andExpect(jsonPath("$.sonidos.length()").value(1))
                .andExpect(jsonPath("$.sonidos[0]").value("pasos"));
        assertThat(repository.findById(id).orElseThrow().getVida()).isEqualTo(30);

        mvc.perform(delete(URL + "/" + id)).andExpect(status().isNoContent());
        assertThat(repository.existsById(id)).isFalse();
        mvc.perform(get(URL + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void altaInformaLocation() throws Exception {
        var respuesta = mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isCreated()).andReturn().getResponse();
        Long id = repository.findAll().getFirst().getId();
        assertThat(respuesta.getHeader("Location")).isEqualTo(URL + "/" + id);
    }

    @Test
    void listarSinRegistrosDevuelveListaVacia() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void idInexistenteDevuelve404EnLecturaActualizacionYBorrado() throws Exception {
        mvc.perform(get(URL + "/999999")).andExpect(status().isNotFound());
        mvc.perform(put(URL + "/999999").contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isNotFound());
        mvc.perform(delete(URL + "/999999")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("datosInvalidos")
    void validacionDeNegociosRechazaAltaSinPersistir(String datos) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
        assertThat(repository.count()).isZero();
    }

    static Stream<String> datosInvalidos() {
        return Stream.of(
                DATOS.replace("Zombie", " "),
                DATOS.replace("Zombie", "x".repeat(256)),
                DATOS.replace("\"vida\":20", "\"vida\":0"),
                DATOS.replace("\"vida\":20", "\"vida\":null"),
                DATOS.replace("\"danoBase\":5", "\"danoBase\":-1"),
                DATOS.replace("\"velocidadMovimiento\":0.7", "\"velocidadMovimiento\":-0.1"),
                DATOS.replace("\"velocidadMovimiento\":0.7", "\"velocidadMovimiento\":1e309"),
                DATOS.replace("\"ancho\":0.6", "\"ancho\":0"),
                DATOS.replace("\"alto\":1.95", "\"alto\":null"),
                DATOS.replace("[\"gruñido\",\"gemido\"]", "null"),
                DATOS.replace("[\"carne_podrida\"]", "[null]"));
    }

    @Test
    void actualizacionInvalidaNoModificaDatosPersistidos() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isCreated());
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(DATOS.replace("\"vida\":20", "\"vida\":-1")))
                .andExpect(status().isBadRequest());
        assertThat(repository.findById(id).orElseThrow().getVida()).isEqualTo(20);
    }

    @Test
    void jsonMalformadoDevuelve400() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }
}
