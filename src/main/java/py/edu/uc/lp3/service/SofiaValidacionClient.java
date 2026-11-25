package py.edu.uc.lp3.service;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import py.edu.uc.lp3.rest.dto.JugadaCruzadaDatos;
import py.edu.uc.lp3.exceptions.ValidacionCompaneroException;

@Component
public class SofiaValidacionClient {
    private final WebClient client;
    private final Duration timeout;

    public SofiaValidacionClient(@Qualifier("sofiaWebClient") WebClient client,
            @Value("${poo09.request-timeout:10s}") Duration timeout) {
        this.client = client;
        this.timeout = timeout;
    }

    public record Solicitud(UUID creeperId, UUID jugadorId, UUID cerdoId) { }
    public record Aprobacion(Boolean aceptada, UUID creeperId, UUID jugadorId, UUID cerdoId) { }

    public void validar(JugadaCruzadaDatos datos) {
        var respuesta = ValidacionRemotaHttp.recibir(client.post().uri("/api/explosiones/validar")
                .bodyValue(new Solicitud(datos.creeperSofiaId(), datos.jugadorSofiaId(), datos.cerdoSofiaId())),
                Aprobacion.class, "A2_SOFIA", timeout);
        if (respuesta.aceptada() == null
                || !datos.creeperSofiaId().equals(respuesta.creeperId())
                || !datos.jugadorSofiaId().equals(respuesta.jugadorId())
                || !datos.cerdoSofiaId().equals(respuesta.cerdoId())) {
            throw ValidacionRemotaHttp.invalida("A2_SOFIA");
        }
        if (!respuesta.aceptada()) {
            throw new ValidacionCompaneroException("A2_SOFIA", 409, "RECHAZO_COMPANERO",
                    "Sofía no acepta los participantes de la explosión");
        }
    }
}
