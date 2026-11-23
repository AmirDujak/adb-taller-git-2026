package py.edu.uc.lp3.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Inicio", description = "Información de bienvenida")
public class IndexController {

    @GetMapping("/")
    @Operation(summary = "Obtener el mensaje de bienvenida")
    @ApiResponse(responseCode = "200", description = "Texto de bienvenida y rutas de ejemplo")
    public String index() {
        return "Bienvenido al taller de Git 2026 - LP3. "
                + "Probá GET /entidad/{tipo} (ej: /entidad/zombie o /entidad/aldeano?profesion=Herrero) o GET /entidades";
    }
}
