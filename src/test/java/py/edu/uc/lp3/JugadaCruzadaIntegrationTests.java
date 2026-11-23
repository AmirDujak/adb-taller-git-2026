package py.edu.uc.lp3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import py.edu.uc.lp3.domain.JugadaCruzada;
import py.edu.uc.lp3.repository.JugadaCruzadaRepository;
import py.edu.uc.lp3.rest.dto.JugadaCruzadaDatos;
import py.edu.uc.lp3.service.JugadaCruzadaService;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:jugadas-test;DB_CLOSE_DELAY=-1")
class JugadaCruzadaIntegrationTests {
    private static final String URL = "/api/lp3/jugadas-cruzadas";
    private static final UUID C = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID J = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID P = UUID.fromString("33333333-3333-4333-8333-333333333333");
    private static final UUID N = UUID.fromString("44444444-4444-4444-8444-444444444444");
    private static final JugadaCruzadaDatos DATOS = new JugadaCruzadaDatos(C, J, P, N);
    private static final List<String> CONSULTAS = new CopyOnWriteArrayList<>();
    private static volatile int estadoSofia = 200;
    private static volatile int estadoNicolas = 200;
    private static volatile String cuerpoSofia;
    private static volatile String cuerpoNicolas;
    private static volatile String enviadoSofia;
    private static final HttpServer SOFIA = servidor(true);
    private static final HttpServer NICOLAS = servidor(false);

    private static HttpServer servidor(boolean sofia) {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                CONSULTAS.add((sofia ? "S " : "N ") + exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());
                if (sofia) enviadoSofia = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                int estado = sofia ? estadoSofia : estadoNicolas;
                if (estado == 0) return; // Conexión aceptada, sin respuesta: timeout real.
                byte[] bytes = (sofia ? cuerpoSofia : cuerpoNicolas).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(estado, bytes.length);
                try (var out = exchange.getResponseBody()) { out.write(bytes); }
            });
            server.start();
            return server;
        } catch (IOException e) { throw new IllegalStateException(e); }
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("poo09.sofia.base-url", () -> "http://127.0.0.1:" + SOFIA.getAddress().getPort());
        registry.add("poo09.nicolas.base-url", () -> "http://127.0.0.1:" + NICOLAS.getAddress().getPort());
        registry.add("poo09.request-timeout", () -> "2s");
    }

    @AfterAll
    static void detener() { SOFIA.stop(0); NICOLAS.stop(0); }

    @Autowired private WebApplicationContext context;
    @Autowired private JugadaCruzadaService service;
    @Autowired private JdbcTemplate jdbc;
    @MockitoSpyBean private JugadaCruzadaRepository repository;
    private MockMvc mvc;
    private String antes;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
        repository.saveAndFlush(new JugadaCruzada(C, J, P, N, true, true));
        estadoSofia = estadoNicolas = 200;
        cuerpoSofia = "{\"aceptada\":true,\"creeperId\":\"%s\",\"jugadorId\":\"%s\",\"cerdoId\":\"%s\"}".formatted(C, J, P);
        cuerpoNicolas = "{\"id\":\"%s\",\"puedeExplotar\":true,\"clase\":\"Creeper\",\"interfaceDominio\":\"Explosivo\"}".formatted(N);
        CONSULTAS.clear();
        antes = snapshot();
    }

    private String snapshot() {
        return List.of("jugadas_cruzadas", "entidades", "entidad_sonidos", "entidad_drops").stream()
                .map(tabla -> jdbc.queryForList("SELECT * FROM " + tabla).toString())
                .toList().toString();
    }

    private String json() throws Exception { return new ObjectMapper().writeValueAsString(DATOS); }

    private void sinCambios() { assertThat(snapshot()).isEqualTo(antes); }

    @Test
    void aceptaPersisteYRecuperaEnOrdenSinModificarEntidades() throws Exception {
        String entidades = jdbc.queryForList("SELECT * FROM entidades").toString();
        var result = mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aceptadaSofia").value(true))
                .andExpect(jsonPath("$.puedeExplotarNicolas").value(true)).andReturn();
        String location = result.getResponse().getHeader("Location");
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.creeperSofiaId").value(C.toString()));
        mvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        assertThat(CONSULTAS).containsExactly("S POST /api/explosiones/validar", "N GET /api/minecraft/explosivos/" + N);
        assertThat(new ObjectMapper().readTree(enviadoSofia).path("creeperId").asText()).isEqualTo(C.toString());
        assertThat(jdbc.queryForList("SELECT * FROM entidades").toString()).isEqualTo(entidades);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 409})
    void rechazoSofiaNoConsultaNicolasNiEscribe(int estado) throws Exception {
        estadoSofia = estado;
        cuerpoSofia = "{\"error\":\"Participantes rechazados\"}";
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().is(estado == 404 ? 404 : 409))
                .andExpect(jsonPath("$.proveedor").value("A2_SOFIA"))
                .andExpect(jsonPath("$.error").value("Participantes rechazados"));
        assertThat(CONSULTAS).hasSize(1);
        sinCambios();
    }

    @Test
    void sofiaAceptaPeroNicolasDevuelveFalse() throws Exception {
        cuerpoNicolas = "{\"id\":\"%s\",\"puedeExplotar\":false}".formatted(N);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.proveedor").value("A3_NICOLAS"));
        assertThat(CONSULTAS).hasSize(2);
        sinCambios();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 409})
    void rechazoHttpNicolas(int estado) throws Exception {
        estadoNicolas = estado;
        cuerpoNicolas = "{\"error\":\"No implementa Explosivo\"}";
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().is(estado == 404 ? 404 : 409))
                .andExpect(jsonPath("$.proveedor").value("A3_NICOLAS"));
        sinCambios();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void timeoutAtribuidoSinEscrituras(boolean sofia) throws Exception {
        if (sofia) estadoSofia = 0; else estadoNicolas = 0;
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.proveedor").value(sofia ? "A2_SOFIA" : "A3_NICOLAS"));
        sinCambios();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void falloHttpNoSeConfundeConRechazo(boolean sofia) throws Exception {
        if (sofia) estadoSofia = 500; else estadoNicolas = 503;
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.proveedor").value(sofia ? "A2_SOFIA" : "A3_NICOLAS"));
        sinCambios();
    }

    @Test
    void sofiaNoAceptaAunqueDevuelva200() throws Exception {
        cuerpoSofia = cuerpoSofia.replace("true", "false");
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.proveedor").value("A2_SOFIA"));
        assertThat(CONSULTAS).hasSize(1);
        sinCambios();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void estadoHttpNoDocumentadoNoSeAcepta(boolean sofia) throws Exception {
        if (sofia) estadoSofia = 201; else estadoNicolas = 201;
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.proveedor").value(sofia ? "A2_SOFIA" : "A3_NICOLAS"));
        sinCambios();
    }

    @Test
    void conexionRechazadaSeAtribuyeAlProveedor() throws Exception {
        int puerto;
        try (var socket = new java.net.ServerSocket(0)) { puerto = socket.getLocalPort(); }
        var client = org.springframework.web.reactive.function.client.WebClient.builder()
                .baseUrl("http://127.0.0.1:" + puerto).build();
        var sofia = new py.edu.uc.lp3.service.SofiaValidacionClient(client, java.time.Duration.ofSeconds(2));
        var nicolas = new py.edu.uc.lp3.service.NicolasValidacionClient(client, java.time.Duration.ofSeconds(2));
        for (var proveedor : List.of("A2_SOFIA", "A3_NICOLAS")) {
            assertThatThrownBy(() -> {
                if (proveedor.equals("A2_SOFIA")) sofia.validar(DATOS); else nicolas.validar(N);
            }).isInstanceOfSatisfying(py.edu.uc.lp3.exceptions.ValidacionCompaneroException.class, e -> {
                assertThat(e.getEstado()).isEqualTo(502);
                assertThat(e.getProveedor()).isEqualTo(proveedor);
            });
        }
        sinCambios();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "no es json", "{\"aceptada\":true,\"creeperId\":\"44444444-4444-4444-8444-444444444444\"}"})
    void contratoSofiaInvalido(String cuerpo) throws Exception {
        cuerpoSofia = cuerpo;
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.proveedor").value("A2_SOFIA"));
        sinCambios();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{\"id\":\"11111111-1111-4111-8111-111111111111\",\"puedeExplotar\":true}"})
    void contratoNicolasInvalido(String cuerpo) throws Exception {
        cuerpoNicolas = cuerpo;
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json()))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.proveedor").value("A3_NICOLAS"));
        sinCambios();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"creeperSofiaId\":\"no-uuid\"}", ""})
    void entradaInvalidaNoConsultaNiEscribe(String cuerpo) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isBadRequest());
        assertThat(CONSULTAS).isEmpty();
        sinCambios();
    }

    @Test
    void falloTrasInsertarRevierteLaTransaccion() {
        doAnswer(invocation -> {
            repository.save(invocation.getArgument(0));
            repository.flush();
            throw new IllegalStateException("Fallo de persistencia simulado después del INSERT");
        }).when(repository).saveAndFlush(any(JugadaCruzada.class));
        assertThatThrownBy(() -> service.ejecutar(DATOS)).isInstanceOf(IllegalStateException.class);
        sinCambios();
    }

    @Test
    void jugadaInexistenteDevuelve404() throws Exception {
        mvc.perform(get(URL + "/999999999")).andExpect(status().isNotFound());
    }
}
