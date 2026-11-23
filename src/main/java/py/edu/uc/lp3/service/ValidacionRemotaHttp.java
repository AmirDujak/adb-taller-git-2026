package py.edu.uc.lp3.service;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;
import py.edu.uc.lp3.exceptions.ValidacionCompaneroException;

/** Solo comparte transporte y traducción de errores; no contiene reglas de Minecraft. */
final class ValidacionRemotaHttp {
    private ValidacionRemotaHttp() { }

    static <T> T recibir(WebClient.RequestHeadersSpec<?> solicitud, Class<T> tipo,
            String proveedor, Duration timeout) {
        return solicitud.retrieve()
                .onStatus(estado -> estado.value() != 200, respuesta -> respuesta.createException())
                .bodyToMono(tipo)
                .switchIfEmpty(Mono.error(invalida(proveedor)))
                .timeout(timeout)
                .onErrorMap(error -> {
                    if (error instanceof ValidacionCompaneroException) return error;
                    if (error instanceof TimeoutException) {
                        return new ValidacionCompaneroException(proveedor, 504, "COMPANERO_TIMEOUT",
                                "Se agotó el tiempo de espera de " + proveedor);
                    }
                    if (error instanceof WebClientResponseException http) {
                        int estado = http.getStatusCode().value();
                        if (estado == 400 || estado == 409 || estado == 404) {
                            return new ValidacionCompaneroException(proveedor, estado == 404 ? 404 : 409,
                                    "RECHAZO_COMPANERO", motivo(http, proveedor));
                        }
                    }
                    return new ValidacionCompaneroException(proveedor, 502, "COMPANERO_NO_DISPONIBLE",
                            "Fallo de conexión, HTTP o contrato de " + proveedor);
                }).block();
    }

    private static String motivo(WebClientResponseException http, String proveedor) {
        try {
            var error = new ObjectMapper().readTree(http.getResponseBodyAsString()).path("error");
            if (error.isString() && !error.asText().isBlank()) {
                // Solo el mensaje público del contrato, no todo el cuerpo del proveedor.
                return error.asText().substring(0, Math.min(500, error.asText().length()));
            }
        } catch (Exception ignored) {
            // Otros formatos se traducen sin exponer detalles internos.
        }
        return proveedor + " rechazó la consulta (HTTP " + http.getStatusCode().value() + ")";
    }

    static ValidacionCompaneroException invalida(String proveedor) {
        return new ValidacionCompaneroException(proveedor, 502, "CONTRATO_COMPANERO_INVALIDO",
                proveedor + " devolvió una respuesta incompleta o incoherente");
    }
}
