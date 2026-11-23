package py.edu.uc.lp3.service.impl;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import py.edu.uc.lp3.exceptions.ConsultaObjetosException;
import py.edu.uc.lp3.rest.dto.ObjetoDatos;
import py.edu.uc.lp3.service.ObjetoService;

@Service
public class ObjetoServiceImpl implements ObjetoService {

    private final WebClient webClient;
    private final Duration requestTimeout;

    public ObjetoServiceImpl(@Qualifier("objetosWebClient") WebClient webClient,
            @Value("${restful-api.request-timeout:10s}") Duration requestTimeout) {
        this.webClient = webClient;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public List<ObjetoDatos> listar() {
        // La aplicación es MVC: se espera la respuesta solamente en el límite del servicio.
        return webClient.get()
                .uri("/objects")
                .retrieve()
                .bodyToFlux(ObjetoDatos.class)
                .collectList()
                .timeout(requestTimeout)
                .onErrorMap(causa -> new ConsultaObjetosException(
                        "No se pudo consultar la API externa de objetos", causa))
                .block();
    }
}
