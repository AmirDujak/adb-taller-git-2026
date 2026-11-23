package py.edu.uc.lp3;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import py.edu.uc.lp3.domain.Creeper;
import py.edu.uc.lp3.exceptions.ConsultaMinecraftException;
import py.edu.uc.lp3.exceptions.ConsultaMinecraftException.Motivo;
import py.edu.uc.lp3.exceptions.ExplosionNoPermitidaException;
import py.edu.uc.lp3.rest.dto.ExplosionIntegradaDatos;
import py.edu.uc.lp3.service.EntidadRemotaService;
import py.edu.uc.lp3.service.EntidadService;
import py.edu.uc.lp3.service.impl.EntidadRemotaServiceImpl;
import py.edu.uc.lp3.service.impl.ExplosionIntegradaServiceImpl;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExplosionIntegradaServiceTests {

    @Test
    void noConsultaLaRedSiElCreeperEstaMuerto() {
        var entidades = mock(EntidadService.class);
        var remotas = mock(EntidadRemotaService.class);
        var creeper = new Creeper();
        creeper.setVida(0);
        when(entidades.buscar(1L)).thenReturn(creeper);
        var servicio = new ExplosionIntegradaServiceImpl(entidades, remotas, new py.edu.uc.lp3.service.ReglasJuegoService());

        assertThrows(ExplosionNoPermitidaException.class, () -> servicio.previsualizar(
                new ExplosionIntegradaDatos(1L, UUID.randomUUID(), UUID.randomUUID())));
        verifyNoInteractions(remotas);
        assertEquals(0, creeper.getVida());
    }

    @Test
    void traduceElFalloDeConexionSinExponerDetallesInternos() {
        var webClient = WebClient.builder().exchangeFunction(request -> Mono.error(
                new IOException("Detalle privado de la conexión"))).build();
        var servicio = new EntidadRemotaServiceImpl(webClient, Duration.ofSeconds(1));

        var error = assertThrows(ConsultaMinecraftException.class, () -> servicio.buscar(UUID.randomUUID()));
        assertEquals(Motivo.NO_DISPONIBLE, error.getMotivo());
        assertFalse(error.getMessage().contains("Detalle privado"));
        assertNotNull(error.getCause());
    }
}
