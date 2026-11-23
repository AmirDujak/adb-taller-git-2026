package py.edu.uc.lp3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI minecraftOpenApi() {
        Schema<?> error = new ObjectSchema()
                .addProperty("codigo", new StringSchema().example("ESTADO_ENTIDAD_INVALIDO"))
                .addProperty("error", new StringSchema().example("La vida inicial debe ser positiva"))
                .addRequiredItem("codigo").addRequiredItem("error");
        return new OpenAPI()
                .info(new Info().title("Minecraft - API REST LP3").version("0.0.1-SNAPSHOT")
                        .description("Entidades persistidas en H2, demostraciones de comportamiento y explosiones. "
                                + "Las rutas alternativas se mantienen por compatibilidad. "
                                + "Crear, actualizar, eliminar y simular modifican los datos persistidos; aplicar-integrada modifica la salud remota. "
                                + "Previsualizar-integrada es de solo lectura, aunque usa POST."))
                .components(new Components().addSchemas("ErrorJuego", error)
                        .addResponses("DatosInvalidos", error("Datos inválidos o JSON ausente/malformado"))
                        .addResponses("EntidadNoEncontrada", error("No existe una entidad con el ID indicado"))
                        .addResponses("ExplosionNoPermitida", error("El origen no es un Creeper o no tiene posición"))
                        .addResponses("ReglaJuego", error("No se cumple una regla del juego"))
                        .addResponses("ParticipanteNoEncontrado", error("No existe el Creeper local o uno de los participantes remotos"))
                        .addResponses("ApiMinecraftNoDisponible", error("Error HTTP, conexión o respuesta inválida del proyecto remoto"))
                        .addResponses("ApiMinecraftTimeout", error("El proyecto remoto superó el tiempo de espera")));
    }

    private ApiResponse error(String descripcion) {
        return new ApiResponse().description(descripcion).content(new Content()
                .addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ErrorJuego"))));
    }
}
