package py.edu.uc.lp3;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class OpenApiIntegrationTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void documentaTodasLasOperacionesRestIncluidasLasRutasAlternativas() throws Exception {
        String json = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("Minecraft - API REST LP3"))
                .andReturn().getResponse().getContentAsString();
        var documento = new ObjectMapper().readTree(json);
        var paths = documento.path("paths");
        Set<String> operationIds = new HashSet<>();
        var mapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        mapping.getHandlerMethods().forEach((info, handler) -> {
            if (!handler.getBeanType().getPackageName().equals("py.edu.uc.lp3.rest.controller")
                    || !handler.getBeanType().isAnnotationPresent(RestController.class)) {
                return;
            }
            for (String ruta : info.getPatternValues()) {
                for (var metodo : info.getMethodsCondition().getMethods()) {
                    var operacion = paths.path(ruta).path(metodo.name().toLowerCase(java.util.Locale.ROOT));
                    assertThat(operacion.isMissingNode()).as("%s %s", metodo, ruta).isFalse();
                    assertThat(operacion.path("summary").asText()).isNotBlank();
                    assertThat(operacion.path("responses").isEmpty()).isFalse();
                    assertThat(operationIds.add(operacion.path("operationId").asText()))
                            .as("operationId único para %s %s", metodo, ruta).isTrue();
                }
            }
        });
        assertThat(operationIds).hasSize(24);
        assertThat(documento.at("/components/schemas/EntidadDatos/required").toString())
                .contains("nombre", "vida", "hitbox", "sonidos", "drop");
        assertThat(documento.at("/components/schemas/ExplosionDatos/properties/creeperId/example").asInt()).isEqualTo(1);
        assertThat(documento.at("/components/schemas/Creeper").isMissingNode()).isFalse();
        var respuestas = paths.path("/api/lp3/entidades/{id}").path("delete").path("responses");
        assertThat(respuestas.path("204").has("content")).isFalse();
        assertThat(respuestas.path("404").path("$ref").asText())
                .isEqualTo("#/components/responses/EntidadNoEncontrada");
        var integrada = paths.path("/api/lp3/explosiones/previsualizar-integrada").path("post");
        assertThat(integrada.path("description").asText()).contains("No consume al Creeper", "LOCALES");
        assertThat(integrada.path("responses").has("502")).isTrue();
        assertThat(integrada.path("responses").has("504")).isTrue();
        assertThat(paths.path("/api/lp3/explosiones/aplicar-integrada").path("post")
                .path("responses").has("207")).isTrue();
        assertThat(documento.at("/components/schemas/ExplosionIntegradaDatos/properties/jugadorExternoId/format").asText())
                .isEqualTo("uuid");
    }

    @Test
    void publicaSwaggerUiYSuConfiguracion() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Swagger UI")));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
        mvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isOk());
    }
}
