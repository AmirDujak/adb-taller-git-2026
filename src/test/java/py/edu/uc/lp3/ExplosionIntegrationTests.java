package py.edu.uc.lp3;

import java.util.List;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.domain.EntidadRegistro;
import py.edu.uc.lp3.domain.HitboxRegistro;
import py.edu.uc.lp3.domain.Posicion;
import py.edu.uc.lp3.domain.TipoEntidad;
import py.edu.uc.lp3.repository.EntidadRepository;
import py.edu.uc.lp3.rest.dto.ExplosionDatos;
import py.edu.uc.lp3.service.ExplosionService;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ExplosionIntegrationTests {

    private static final String URL = ApiPaths.EXPLOSIONES + "/simular";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private EntidadRepository repository;
    @Autowired
    private ExplosionService service;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    private EntidadRegistro guardar(String nombre, int vida, boolean cargado, Posicion posicion) {
        return repository.save(new EntidadRegistro(nombre, vida, 5, 0.7,
                new HitboxRegistro(0.6, 1.8, 0.6), List.of("sonido"), List.of("drop"),
                TipoEntidad.desdeNombre(nombre), cargado, posicion));
    }

    private String cuerpo(Long id) {
        return "{\"creeperId\":" + id + "}";
    }

    @Test
    void leePosicionesYVidasDeH2ActualizaSupervivientesYBorraMuertos() throws Exception {
        var creeper = guardar("Creeper", 20, false, new Posicion(10, 20, 30));
        var player = guardar("Player", 20, false, new Posicion(11, 20, 30));
        var zombie = guardar("Zombie", 5, false, new Posicion(10, 22, 30));
        var lejano = guardar("Oveja", 20, false, new Posicion(10, 20, 34));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo(creeper.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.radio").value(3.0))
                .andExpect(jsonPath("$.creeper.id").value(creeper.getId()))
                .andExpect(jsonPath("$.creeper.eliminado").value(true))
                .andExpect(jsonPath("$.objetivos.length()").value(2))
                .andExpect(jsonPath("$.objetivos[0].id").value(player.getId()))
                .andExpect(jsonPath("$.objetivos[0].distancia").value(1.0))
                .andExpect(jsonPath("$.objetivos[0].vidaInicial").value(20))
                .andExpect(jsonPath("$.objetivos[0].danoRecibido").value(13))
                .andExpect(jsonPath("$.objetivos[0].vidaFinal").value(7))
                .andExpect(jsonPath("$.objetivos[0].eliminado").value(false))
                .andExpect(jsonPath("$.objetivos[1].danoRecibido").value(5))
                .andExpect(jsonPath("$.objetivos[1].distancia").value(2.0))
                .andExpect(jsonPath("$.objetivos[1].vidaFinal").value(0))
                .andExpect(jsonPath("$.objetivos[1].eliminado").value(true));

        assertThat(repository.existsById(creeper.getId())).isFalse();
        assertThat(repository.existsById(zombie.getId())).isFalse();
        assertThat(repository.findById(player.getId()).orElseThrow().getVida()).isEqualTo(7);
        assertThat(repository.findById(lejano.getId()).orElseThrow().getVida()).isEqualTo(20);
        mvc.perform(get(ApiPaths.ENTIDADES + "/" + zombie.getId())).andExpect(status().isNotFound());
        mvc.perform(get(ApiPaths.ENTIDADES + "/" + player.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.vida").value(7));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo(creeper.getId())))
                .andExpect(status().isNotFound());
        assertThat(repository.findById(player.getId()).orElseThrow().getVida()).isEqualTo(7);
    }

    @Test
    void usaCargaPersistidaYDistingueDistanciaEuclidianaDeCajaDeCoordenadas() throws Exception {
        var creeper = guardar("Creeper", 20, true, new Posicion(0, 0, 0));
        var player = guardar("Player", 100, false, new Posicion(0, 0, 0));
        var borde = guardar("Zombie", 20, false, new Posicion(1, 2, 2));
        var diagonal = guardar("Lobo", 20, false, new Posicion(2, 2, 2));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo(creeper.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creeper.cargado").value(true))
                .andExpect(jsonPath("$.objetivos.length()").value(2))
                .andExpect(jsonPath("$.objetivos[0].danoRecibido").value(40))
                .andExpect(jsonPath("$.objetivos[1].distancia").value(3.0))
                .andExpect(jsonPath("$.objetivos[1].danoRecibido").value(0));
        assertThat(repository.findById(player.getId()).orElseThrow().getVida()).isEqualTo(60);
        assertThat(repository.findById(borde.getId()).orElseThrow().getVida()).isEqualTo(20);
        assertThat(repository.findById(diagonal.getId()).orElseThrow().getVida()).isEqualTo(20);
    }

    @Test
    void conservaRegistrosSinPosicionYNoInventaDistancias() throws Exception {
        var creeper = guardar("Creeper", 20, false, new Posicion(0, 0, 0));
        var antiguo = guardar("Zombie", 20, false, null);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo(creeper.getId())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.objetivos").isEmpty());
        assertThat(repository.findById(antiguo.getId()).orElseThrow().getVida()).isEqualTo(20);
    }

    @Test
    void noModificaNadaSiElOrigenEsInvalido() throws Exception {
        var noCreeper = guardar("Zombie", 20, false, new Posicion(0, 0, 0));
        var sinPosicion = guardar("Creeper", 20, false, null);
        for (Long id : List.of(noCreeper.getId(), sinPosicion.getId())) {
            mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo(id)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").isNotEmpty());
        }
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo(999999L)))
                .andExpect(status().isNotFound());
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findAll()).allMatch(e -> e.getVida() == 20);
    }

    @Test
    void cambiosYEliminacionesSeReviertenJuntosAnteUnError() {
        var creeper = guardar("Creeper", 20, false, new Posicion(0, 0, 0));
        var player = guardar("Player", 20, false, new Posicion(1, 0, 0));
        var zombie = guardar("Zombie", 5, false, new Posicion(0, 0, 0));
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);
        assertThatIllegalStateException().isThrownBy(() -> transaccion.executeWithoutResult(status -> {
            service.simular(new ExplosionDatos(creeper.getId()));
            repository.flush();
            throw new IllegalStateException("Fallo antes de confirmar");
        }));
        assertThat(repository.count()).isEqualTo(3);
        assertThat(repository.findById(player.getId()).orElseThrow().getVida()).isEqualTo(20);
        assertThat(repository.findById(zombie.getId()).orElseThrow().getVida()).isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("datosInvalidos")
    void rechazaParametrosInvalidos(String datos) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").isNotEmpty());
        assertThat(repository.count()).isZero();
    }

    static Stream<String> datosInvalidos() {
        return Stream.of("{}", "{\"creeperId\":null}", "{\"creeperId\":0}", "{\"creeperId\":-1}",
                "{\"creeperCargado\":false,\"vidaPlayer\":20,\"distanciaPlayer\":1,"
                        + "\"vidaZombie\":20,\"distanciaZombie\":4}");
    }

    @Test
    void rechazaCuerpoAusenteNuloOMalformado() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("null"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }
}
