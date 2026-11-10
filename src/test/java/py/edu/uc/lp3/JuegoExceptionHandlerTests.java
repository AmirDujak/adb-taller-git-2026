package py.edu.uc.lp3;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import py.edu.uc.lp3.exceptions.*;
import py.edu.uc.lp3.rest.controller.JuegoExceptionHandler;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JuegoExceptionHandlerTests {
    /** Solo fixture de test: no se agrega una ruta de errores a la aplicación. */
    @RestController
    static class Errores {
        @GetMapping("/prueba/{tipo}")
        String lanzar(@PathVariable String tipo) {
            throw switch (tipo) {
                case "estado" -> new EstadoEntidadInvalidoException("Vida inválida");
                case "explosion" -> new ExplosionNoPermitidaException("Sin posición");
                case "inexistente" -> new EntidadNoEncontradaException(123L);
                default -> new JuegoException("Regla del juego incumplida");
            };
        }
    }

    @Test
    void hayUnHandlerEspecificoPorTipoInclusoParaLaBase() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Errores())
                .setControllerAdvice(new JuegoExceptionHandler()).build();
        mvc.perform(get("/prueba/base")).andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_JUEGO"));
        mvc.perform(get("/prueba/estado")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ESTADO_ENTIDAD_INVALIDO"));
        mvc.perform(get("/prueba/explosion")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EXPLOSION_NO_PERMITIDA"));
        mvc.perform(get("/prueba/inexistente")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ENTIDAD_NO_ENCONTRADA"));
    }
}
