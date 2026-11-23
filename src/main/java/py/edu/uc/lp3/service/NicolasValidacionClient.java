package py.edu.uc.lp3.service;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import py.edu.uc.lp3.exceptions.ValidacionCompaneroException;

@Component
public class NicolasValidacionClient {
    private final WebClient client;
    private final Duration timeout;

    public NicolasValidacionClient(@Qualifier("nicolasWebClient") WebClient client,
            @Value("${poo09.request-timeout:10s}") Duration timeout) {
        this.client = client;
        this.timeout = timeout;
    }

    public record Aprobacion(UUID id, Boolean puedeExplotar) { }

    public void validar(UUID id) {
        var respuesta = ValidacionRemotaHttp.recibir(client.get().uri("/api/minecraft/explosivos/{id}", id),
                Aprobacion.class, "A3_NICOLAS", timeout);
        if (!id.equals(respuesta.id()) || respuesta.puedeExplotar() == null) {
            throw ValidacionRemotaHttp.invalida("A3_NICOLAS");
        }
        if (!respuesta.puedeExplotar()) {
            throw new ValidacionCompaneroException("A3_NICOLAS", 409, "RECHAZO_COMPANERO",
                    "Nicolás informa que el mob no puede explotar");
        }
    }
}
