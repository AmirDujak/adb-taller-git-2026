package py.edu.uc.lp3.service.impl;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import py.edu.uc.lp3.exceptions.ConsultaMinecraftException;
import py.edu.uc.lp3.exceptions.ConsultaMinecraftException.Motivo;
import py.edu.uc.lp3.rest.dto.EntidadRemotaDatos;
import py.edu.uc.lp3.rest.dto.ActualizarSaludRemotaDatos;
import py.edu.uc.lp3.service.EntidadRemotaService;
import reactor.core.publisher.Mono;

@Service
public class EntidadRemotaServiceImpl implements EntidadRemotaService {

    private final WebClient webClient;
    private final Duration requestTimeout;

    public EntidadRemotaServiceImpl(@Qualifier("minecraftWebClient") WebClient webClient,
            @Value("${minecraft-api.request-timeout:10s}") Duration requestTimeout) {
        this.webClient = webClient;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public EntidadRemotaDatos buscar(UUID id) {
        return recibir(webClient.get().uri("/api/entidades/{id}", id).retrieve(), id);
    }

    @Override
    public EntidadRemotaDatos actualizarSalud(UUID id, ActualizarSaludRemotaDatos datos) {
        return recibir(webClient.put().uri("/api/entidades/{id}", id)
                .bodyValue(datos).retrieve(), id);
    }

    private EntidadRemotaDatos recibir(WebClient.ResponseSpec respuesta, UUID id) {
        return respuesta
                .bodyToMono(EntidadRemotaDatos.class)
                .switchIfEmpty(Mono.error(new IllegalStateException("Respuesta remota vacía")))
                .timeout(requestTimeout)
                .onErrorMap(causa -> {
                    if (causa instanceof TimeoutException) {
                        return new ConsultaMinecraftException(Motivo.TIEMPO_AGOTADO,
                                "La API Minecraft remota superó el tiempo de espera", causa);
                    }
                    if (causa instanceof WebClientResponseException http && http.getStatusCode().value() == 404) {
                        return new ConsultaMinecraftException(Motivo.NO_ENCONTRADA,
                                "No existe la entidad remota " + id, causa);
                    }
                    return new ConsultaMinecraftException(Motivo.NO_DISPONIBLE,
                            "No se pudo consultar la API Minecraft remota", causa);
                })
                .block();
    }
}
