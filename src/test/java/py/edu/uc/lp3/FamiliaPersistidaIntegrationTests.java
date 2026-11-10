package py.edu.uc.lp3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import py.edu.uc.lp3.domain.*;
import py.edu.uc.lp3.repository.EntidadRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class FamiliaPersistidaIntegrationTests {
    private static final String URL = "/api/lp3/entidades";
    private static final String DATOS = """
            {"nombre":"Personalizado","tipo":"%s","vida":20,"danoBase":5,
             "velocidadMovimiento":1,"hitbox":{"ancho":1,"alto":1,"profundidad":1},
             "sonidos":[],"drop":[],"posicion":{"x":0,"y":64,"z":0}}
            """;
    @Autowired private WebApplicationContext context;
    @Autowired private EntidadRepository repository;
    @Autowired private PlatformTransactionManager transactionManager;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void todosLosSubtiposSeRecuperanComoClasesReales() throws Exception {
        for (TipoEntidad tipo : TipoEntidad.values()) {
            mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(DATOS.formatted(tipo)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.tipo").value(tipo.name()));
        }
        var entidades = repository.findAll();
        assertThat(entidades).hasSize(10);
        assertThat(entidades).anyMatch(e -> e instanceof Creeper);
        assertThat(entidades).anyMatch(e -> e instanceof Player);
        assertThat(entidades).anyMatch(e -> e instanceof Zombie);
        assertThat(entidades.stream().map(Object::getClass).distinct().count()).isEqualTo(10);
        mvc.perform(get(URL + "/movimientos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].avanzar").isString());
        assertThat(repository.findAll()).allMatch(e -> e.getVida() == 20);
    }

    @Test
    void dosHijasTienenHandlersDistintosYNoAlteranLaBase() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(DATOS.formatted("PLAYER")))
                .andExpect(status().isCreated());
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(DATOS.formatted("ZOMBIE").replace("\"vida\":20", "\"vida\":0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ESTADO_ENTIDAD_INVALIDO"));
        mvc.perform(post("/api/lp3/explosiones/simular").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"creeperId\":" + id + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EXPLOSION_NO_PERMITIDA"));
        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(id).orElseThrow().getVida()).isEqualTo(20);
    }

    @Test
    void putNoPuedeTransformarUnPlayerEnCreeper() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(DATOS.formatted("PLAYER")))
                .andExpect(status().isCreated());
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(DATOS.formatted("CREEPER")))
                .andExpect(status().isBadRequest());
        assertThat(repository.findById(id).orElseThrow()).isInstanceOf(Player.class);
    }

    @Test
    void jpaNoGuardaUnaEntidadRetiradaSinPasarPorElService() {
        Player retirado = new Player();
        retirado.retirarDelMundo();
        var transaction = new TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> repository.saveAndFlush(retirado)))
                .hasStackTraceContaining("La vida inicial debe ser positiva");
        assertThat(repository.count()).isZero();
    }

    @Test
    void constructorDelZombiePequenoNoPierdeElSubtipoAlPersistir() {
        Long id = repository.saveAndFlush(new ZombiePequeño()).getId();
        assertThat(repository.findById(id).orElseThrow()).isInstanceOf(ZombiePequeño.class);
    }
}
