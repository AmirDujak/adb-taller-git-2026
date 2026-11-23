package py.edu.uc.lp3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import py.edu.uc.lp3.exceptions.ConsultaObjetosException;
import py.edu.uc.lp3.service.impl.ObjetoServiceImpl;
import static org.junit.jupiter.api.Assertions.*;

class ObjetoServiceTests {

    private HttpServer server;
    private WebClient webClient;
    private String metodo;
    private String ruta;

    @BeforeEach
    void iniciarServidor() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        webClient = WebClient.builder()
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build();
    }

    @AfterEach
    void detenerServidor() {
        server.stop(0);
    }

    private void responder(int estado, String cuerpo) {
        server.createContext("/objects", exchange -> {
            metodo = exchange.getRequestMethod();
            ruta = exchange.getRequestURI().toString();
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(estado, bytes.length);
            try (var salida = exchange.getResponseBody()) {
                salida.write(bytes);
            }
        });
    }

    @Test
    void consultaObjetosConDatosVariablesYNulos() {
        responder(200, """
                [{"id":"1","name":"Pixel","data":{"color":"White","capacity GB":512}},
                 {"id":"2","name":"iPhone","data":null}]
                """);
        var objetos = new ObjetoServiceImpl(webClient, Duration.ofSeconds(5)).listar();

        assertEquals("GET", metodo);
        assertEquals("/objects", ruta);
        assertEquals(2, objetos.size());
        assertEquals("1", objetos.getFirst().id());
        assertEquals("Pixel", objetos.getFirst().name());
        assertEquals("White", objetos.getFirst().data().get("color"));
        assertEquals(512, objetos.getFirst().data().get("capacity GB"));
        assertNull(objetos.get(1).data());
    }

    @Test
    void admiteListaVacia() {
        responder(200, "[]");
        assertTrue(new ObjetoServiceImpl(webClient, Duration.ofSeconds(5)).listar().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 500})
    void conservaLaCausaDeErroresHttp(int estado) {
        responder(estado, "{\"error\":\"Error del proveedor\"}");
        var servicio = new ObjetoServiceImpl(webClient, Duration.ofSeconds(5));

        var error = assertThrows(ConsultaObjetosException.class, servicio::listar);
        var causa = assertInstanceOf(WebClientResponseException.class, error.getCause());
        assertEquals(estado, causa.getStatusCode().value());
    }

    @Test
    void limitaElTiempoDeEspera() {
        // El servidor acepta la consulta pero no envía una respuesta.
        server.createContext("/objects", exchange -> { });
        var servicio = new ObjetoServiceImpl(webClient, Duration.ofMillis(100));

        var error = assertThrows(ConsultaObjetosException.class, servicio::listar);
        assertInstanceOf(TimeoutException.class, error.getCause());
    }
}
