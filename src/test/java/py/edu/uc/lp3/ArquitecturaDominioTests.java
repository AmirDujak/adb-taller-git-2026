package py.edu.uc.lp3;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Regresión de la observación 2 de POO-08: el dominio no aplica reglas ni lanza excepciones. */
class ArquitecturaDominioTests {
    @Test
    void dominioNoDependeDeExcepcionesServiciosNiCallbacksDeValidacion() throws Exception {
        try (var archivos = Files.list(Path.of("src/main/java/py/edu/uc/lp3/domain"))) {
            for (var archivo : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                String codigo = Files.readString(archivo);
                assertThat(codigo).as(archivo.toString())
                        .doesNotContain("throw new", "py.edu.uc.lp3.exceptions", "py.edu.uc.lp3.service",
                                "@PrePersist", "@PreUpdate", "static void main", "calcularDanioExplosion(",
                                "recibirDanio(", "prepararExplosion(", "String comportamiento(", "distanciaA(");
            }
        }
    }
}
