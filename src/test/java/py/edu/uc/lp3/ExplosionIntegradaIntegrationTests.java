package py.edu.uc.lp3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import py.edu.uc.lp3.domain.Entidad;
import py.edu.uc.lp3.domain.TipoEntidad;
import py.edu.uc.lp3.repository.EntidadRepository;
import py.edu.uc.lp3.rest.dto.EntidadDatos;
import py.edu.uc.lp3.rest.dto.EntidadDatos.HitboxDatos;
import py.edu.uc.lp3.rest.dto.EntidadDatos.PosicionDatos;
import py.edu.uc.lp3.service.EntidadService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ExplosionIntegradaIntegrationTests {

    private static final String URL = "/api/lp3/explosiones/previsualizar-integrada";
    private static final String APLICAR = "/api/lp3/explosiones/aplicar-integrada";
    private static final UUID JUGADOR = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID CERDO = UUID.fromString("33333333-3333-4333-8333-333333333333");
    private static final Map<String, Respuesta> RESPUESTAS = new ConcurrentHashMap<>();
    private static final List<String> CONSULTAS = new CopyOnWriteArrayList<>();
    private static final Map<String, Respuesta> RESPUESTAS_PUT = new ConcurrentHashMap<>();
    private static final Map<String, String> CUERPOS_PUT = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpServer SERVER = iniciarServidor();

    private record Respuesta(int estado, String cuerpo) { }

    private static HttpServer iniciarServidor() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                String ruta = exchange.getRequestURI().getPath();
                CONSULTAS.add(exchange.getRequestMethod() + " " + ruta);
                Respuesta respuesta = RESPUESTAS.getOrDefault(ruta, new Respuesta(404, "{}"));
                if (exchange.getRequestMethod().equals("PUT")) {
                    String cuerpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    CUERPOS_PUT.put(ruta, cuerpo);
                    if (RESPUESTAS_PUT.containsKey(ruta)) {
                        respuesta = RESPUESTAS_PUT.get(ruta);
                    } else if (respuesta.estado() == 200) {
                        var actual = (ObjectNode) MAPPER.readTree(respuesta.cuerpo());
                        actual.put("salud", MAPPER.readTree(cuerpo).path("salud").asDouble());
                        respuesta = new Respuesta(200, actual.toString());
                        RESPUESTAS.put(ruta, respuesta);
                    }
                }
                if (respuesta.estado() == 0) return; // Acepta, pero no responde: timeout real.
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] bytes = respuesta.cuerpo().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(respuesta.estado(), respuesta.estado() == 204 ? -1 : bytes.length);
                try (var salida = exchange.getResponseBody()) {
                    if (respuesta.estado() != 204) salida.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void configurarApiRemota(DynamicPropertyRegistry registry) {
        registry.add("minecraft-api.base-url", () -> "http://127.0.0.1:" + SERVER.getAddress().getPort());
        registry.add("minecraft-api.request-timeout", () -> "1s");
    }

    @AfterAll
    static void detenerServidor() {
        SERVER.stop(0);
    }

    @Autowired private WebApplicationContext context;
    @Autowired private EntidadService entidades;
    @Autowired private EntidadRepository repository;
    private MockMvc mvc;
    private Entidad creeper;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        RESPUESTAS.clear();
        RESPUESTAS_PUT.clear();
        CUERPOS_PUT.clear();
        CONSULTAS.clear();
        repository.deleteAll();
        creeper = crear(TipoEntidad.CREEPER, false, new PosicionDatos(0.0, 64.0, 0.0));
        responder(JUGADOR, entidadJson(JUGADOR, "JUGADOR", "20.5", 1.5));
        responder(CERDO, entidadJson(CERDO, "CERDO", "7.25", 0));
    }

    private Entidad crear(TipoEntidad tipo, boolean cargado, PosicionDatos posicion) {
        return entidades.crear(new EntidadDatos(tipo.name(), 20, 5, 0.7,
                new HitboxDatos(0.6, 1.7, 0.6), List.of("sonido"), List.of("drop"), tipo, cargado, posicion));
    }

    private static String entidadJson(UUID id, String tipo, String salud, double x) {
        return """
                {"id":"%s","nombre":"Participante","tipo":"%s","salud":%s,
                 "ubicacion":{"x":%s,"y":64,"z":0},"atributos":{"modoCreativo":false}}
                """.formatted(id, tipo, salud, x);
    }

    private static void responder(UUID id, String json) {
        RESPUESTAS.put("/api/entidades/" + id, new Respuesta(200, json));
    }

    private String datos(Long id, UUID jugador, UUID cerdo) {
        return """
                {"creeperId":%s,"jugadorExternoId":"%s","cerdoExternoId":"%s"}
                """.formatted(id, jugador, cerdo);
    }

    @Test
    void combinaReglasSinCambiarH2NiEscribirEnLaApiRemota() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reglasAplicadas").value("LOCALES"))
                .andExpect(jsonPath("$.radio").value(3.0))
                .andExpect(jsonPath("$.vidaCreeper").value(20))
                .andExpect(jsonPath("$.objetivos[0].distancia").value(1.5))
                .andExpect(jsonPath("$.objetivos[0].danioCalculado").value(10))
                .andExpect(jsonPath("$.objetivos[0].saludEstimada").value(10.5))
                .andExpect(jsonPath("$.objetivos[1].danioAplicado").value(7.25))
                .andExpect(jsonPath("$.objetivos[1].saludEstimada").value(0.0))
                .andExpect(jsonPath("$.objetivos[1].vivaEstimada").value(false));

        assertThat(CONSULTAS).containsExactly("GET /api/entidades/" + JUGADOR, "GET /api/entidades/" + CERDO);
        Entidad sinCambios = entidades.buscar(creeper.getId());
        assertThat(repository.count()).isEqualTo(1);
        assertThat(sinCambios.getVida()).isEqualTo(20);
        assertThat(sinCambios.isCargado()).isFalse();
        assertThat(sinCambios.getPosicion().getY()).isEqualTo(64.0);
    }

    @Test
    void duplicaElDanioDelCreeperCargado() throws Exception {
        Entidad cargado = crear(TipoEntidad.CREEPER, true, new PosicionDatos(0.0, 64.0, 0.0));
        responder(CERDO, entidadJson(CERDO, "CERDO", "100", 0));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(cargado.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargado").value(true))
                .andExpect(jsonPath("$.objetivos[0].danioCalculado").value(20))
                .andExpect(jsonPath("$.objetivos[1].danioCalculado").value(40));
        assertThat(entidades.buscar(cargado.getId()).getVida()).isEqualTo(20);
        assertThat(entidades.buscar(cargado.getId()).isCargado()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(doubles = {3.0, 4.5, 6.0})
    void noAplicaElRadioRemotoDeSeisBloques(double distancia) throws Exception {
        responder(JUGADOR, entidadJson(JUGADOR, "JUGADOR", "20.5", distancia));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivos[0].danioCalculado").value(0))
                .andExpect(jsonPath("$.objetivos[0].saludEstimada").value(20.5));
    }

    @Test
    void admiteParticipantesSinSalud() throws Exception {
        responder(JUGADOR, entidadJson(JUGADOR, "JUGADOR", "0", 0));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivos[0].danioAplicado").value(0.0))
                .andExpect(jsonPath("$.objetivos[0].vivaEstimada").value(false));
    }

    @Test
    void rechazaIdsRemotosIgualesAntesDeConsultarLaRed() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, JUGADOR)))
                .andExpect(status().isBadRequest());
        assertThat(CONSULTAS).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{", "{\"creeperId\":0}",
            "{\"creeperId\":1,\"jugadorExternoId\":\"no-es-uuid\"}"})
    void rechazaCuerposInvalidos(String json) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        assertThat(CONSULTAS).isEmpty();
    }

    @Test
    void rechazaCuerpoAusente() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        assertThat(CONSULTAS).isEmpty();
    }

    @Test
    void rechazaCreeperLocalInexistenteSinConsultarLaRed() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(Long.MAX_VALUE, JUGADOR, CERDO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ENTIDAD_NO_ENCONTRADA"));
        assertThat(CONSULTAS).isEmpty();
    }

    @Test
    void rechazaOrigenLocalQueNoEsCreeper() throws Exception {
        Entidad jugador = crear(TipoEntidad.PLAYER, false, new PosicionDatos(0.0, 64.0, 0.0));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(jugador.getId(), JUGADOR, CERDO)))
                .andExpect(status().isConflict());
        assertThat(CONSULTAS).isEmpty();
    }

    @Test
    void rechazaCreeperSinPosicion() throws Exception {
        Entidad sinPosicion = crear(TipoEntidad.CREEPER, false, null);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(sinPosicion.getId(), JUGADOR, CERDO)))
                .andExpect(status().isConflict());
        assertThat(CONSULTAS).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CERDO", "ZOMBIE"})
    void validaTipoDelJugadorExterno(String tipo) throws Exception {
        responder(JUGADOR, entidadJson(JUGADOR, tipo, "20", 0));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validaTipoDelCerdoExterno() throws Exception {
        responder(CERDO, entidadJson(CERDO, "JUGADOR", "20", 0));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 500, 503})
    void traduceErroresHttpDelProveedorA502(int estado) throws Exception {
        RESPUESTAS.put("/api/entidades/" + JUGADOR, new Respuesta(estado, "{\"error\":\"interno\"}"));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value("API_MINECRAFT_NO_DISPONIBLE"));
        assertThat(entidades.buscar(creeper.getId()).getVida()).isEqualTo(20);
    }

    @Test
    void distingueUnaEntidadRemotaInexistente() throws Exception {
        RESPUESTAS.remove("/api/entidades/" + CERDO);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ENTIDAD_REMOTA_NO_ENCONTRADA"));
        assertThat(entidades.buscar(creeper.getId()).getVida()).isEqualTo(20);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "[", "null"})
    void rechazaRespuestaRemotaIncompletaOMalformada(String json) throws Exception {
        responder(JUGADOR, json);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadGateway());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "null", "1e400"})
    void rechazaSaludInvalidaDelProveedor(String salud) throws Exception {
        responder(JUGADOR, entidadJson(JUGADOR, "JUGADOR", salud, 0));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void rechazaCoordenadasInvalidasDelProveedor() throws Exception {
        responder(JUGADOR, entidadJson(JUGADOR, "JUGADOR", "20", 0).replace("\"y\":64", "\"y\":null"));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void rechazaIdentidadRemotaInconsistente() throws Exception {
        responder(JUGADOR, entidadJson(CERDO, "JUGADOR", "20", 0));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void rechazaRespuestaVaciaDelProveedor() throws Exception {
        RESPUESTAS.put("/api/entidades/" + JUGADOR, new Respuesta(204, ""));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void limitaElTiempoDeEsperaSinModificarLaVidaLocal() throws Exception {
        RESPUESTAS.put("/api/entidades/" + JUGADOR, new Respuesta(0, ""));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.codigo").value("API_MINECRAFT_TIMEOUT"));
        assertThat(entidades.buscar(creeper.getId()).getVida()).isEqualTo(20);
    }

    @Test
    void aplicaSaludRealConPutYConservaLosDemasDatos() throws Exception {
        responder(JUGADOR, entidadJson(JUGADOR, "JUGADOR", "20", 1));
        responder(CERDO, entidadJson(CERDO, "CERDO", "10", 2));
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completa").value(true))
                .andExpect(jsonPath("$.objetivos[0].estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.objetivos[0].saludConfirmada").value(7.0))
                .andExpect(jsonPath("$.objetivos[1].estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.objetivos[1].saludConfirmada").value(3.0));
        assertThat(CONSULTAS).containsExactly("GET /api/entidades/" + JUGADOR,
                "GET /api/entidades/" + CERDO, "PUT /api/entidades/" + JUGADOR, "PUT /api/entidades/" + CERDO);
        for (UUID id : List.of(JUGADOR, CERDO)) {
            var enviado = MAPPER.readTree(CUERPOS_PUT.get("/api/entidades/" + id));
            assertThat(enviado.path("nombre").asText()).isEqualTo("Participante");
            assertThat(enviado.path("ubicacion").path("y").asDouble()).isEqualTo(64.0);
            assertThat(enviado.has("atributos")).isFalse();
            assertThat(enviado.has("id")).isFalse();
            assertThat(MAPPER.readTree(RESPUESTAS.get("/api/entidades/" + id).cuerpo())
                    .path("atributos").path("modoCreativo").asBoolean()).isFalse();
        }
        assertThat(MAPPER.readTree(RESPUESTAS.get("/api/entidades/" + CERDO).cuerpo())
                .path("salud").asDouble()).isEqualTo(3.0);
        assertThat(entidades.buscar(creeper.getId()).getVida()).isEqualTo(20);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void noEscribeSiElSegundoParticipanteNoEsValido() throws Exception {
        responder(CERDO, entidadJson(CERDO, "JUGADOR", "10", 2));
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isBadRequest());
        assertThat(CUERPOS_PUT).isEmpty();
    }

    @Test
    void noEscribeSiFallaLaSegundaConsulta() throws Exception {
        RESPUESTAS.remove("/api/entidades/" + CERDO);
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isNotFound());
        assertThat(CUERPOS_PUT).isEmpty();
    }

    @Test
    void noEnviaPutCuandoNoHayDanio() throws Exception {
        responder(JUGADOR, entidadJson(JUGADOR, "JUGADOR", "20.5", 4));
        responder(CERDO, entidadJson(CERDO, "CERDO", "0", 0));
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completa").value(true))
                .andExpect(jsonPath("$.objetivos[0].estado").value("SIN_CAMBIOS"))
                .andExpect(jsonPath("$.objetivos[1].estado").value("SIN_CAMBIOS"));
        assertThat(CUERPOS_PUT).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 500, 0})
    void informaAplicacionParcialSinRevertirAlPrimerParticipante(int estado) throws Exception {
        RESPUESTAS_PUT.put("/api/entidades/" + CERDO, new Respuesta(estado, "{}"));
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().is(207))
                .andExpect(jsonPath("$.completa").value(false))
                .andExpect(jsonPath("$.objetivos[0].estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.objetivos[0].saludConfirmada").value(10.5))
                .andExpect(jsonPath("$.objetivos[1].estado").value("NO_CONFIRMADO"))
                .andExpect(jsonPath("$.objetivos[1].saludConfirmada").isEmpty());
        assertThat(CONSULTAS).hasSize(4); // No hay rollback ni reintento.
        assertThat(entidades.buscar(creeper.getId()).getVida()).isEqualTo(20);
    }

    @Test
    void detieneLasEscriturasSiFallaElPrimerPut() throws Exception {
        RESPUESTAS_PUT.put("/api/entidades/" + JUGADOR, new Respuesta(500, "{}"));
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().is(207))
                .andExpect(jsonPath("$.objetivos[0].estado").value("NO_CONFIRMADO"))
                .andExpect(jsonPath("$.objetivos[1].estado").value("NO_INTENTADO"));
        assertThat(CUERPOS_PUT).containsOnlyKeys("/api/entidades/" + JUGADOR);
        assertThat(CONSULTAS).hasSize(3);
    }

    @Test
    void noConfirmaUnPutQueDevuelveUnaSaludDistinta() throws Exception {
        RESPUESTAS_PUT.put("/api/entidades/" + JUGADOR,
                new Respuesta(200, entidadJson(JUGADOR, "JUGADOR", "20.5", 1.5)));
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().is(207))
                .andExpect(jsonPath("$.objetivos[0].estado").value("NO_CONFIRMADO"))
                .andExpect(jsonPath("$.objetivos[1].estado").value("NO_INTENTADO"));
    }

    @Test
    void repetirLaOperacionVuelveACalcularConLaSaludActual() throws Exception {
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk());
        mvc.perform(post(APLICAR).contentType(MediaType.APPLICATION_JSON).content(datos(creeper.getId(), JUGADOR, CERDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivos[0].estimacion.saludAnterior").value(10.5))
                .andExpect(jsonPath("$.objetivos[0].saludConfirmada").value(0.5))
                .andExpect(jsonPath("$.objetivos[1].estado").value("SIN_CAMBIOS"));
    }
}
