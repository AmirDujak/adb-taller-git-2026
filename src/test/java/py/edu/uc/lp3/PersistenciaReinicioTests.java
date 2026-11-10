package py.edu.uc.lp3;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import py.edu.uc.lp3.domain.*;
import py.edu.uc.lp3.rest.dto.EntidadDatos;
import py.edu.uc.lp3.rest.dto.ExplosionDatos;
import py.edu.uc.lp3.service.EntidadService;
import py.edu.uc.lp3.service.ExplosionService;
import static org.assertj.core.api.Assertions.assertThat;

class PersistenciaReinicioTests {
    @TempDir Path carpeta;

    @Test
    void explosionYSubtiposSobrevivenAlCierreYReaperturaDeH2() {
        String[] propiedades = {"--spring.datasource.url=jdbc:h2:file:" + carpeta.resolve("mundo"),
                "--spring.jpa.hibernate.ddl-auto=update", "--spring.h2.console.enabled=false"};
        Long playerId;
        Long creeperId;
        try (var app = new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE)
                .run(propiedades)) {
            var entidades = app.getBean(EntidadService.class);
            creeperId = entidades.crear(datos("Creeper", 0)).getId();
            playerId = entidades.crear(datos("Player", 1)).getId();
            entidades.crear(datos("Zombie", 2));
            app.getBean(ExplosionService.class).simular(new ExplosionDatos(creeperId));
        }
        try (var app = new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE)
                .run(propiedades)) {
            var entidades = app.getBean(EntidadService.class);
            assertThat(entidades.buscar(playerId)).isInstanceOf(Player.class);
            assertThat(entidades.buscar(playerId).getVida()).isEqualTo(7);
            assertThat(entidades.listar()).noneMatch(e -> e.getId().equals(creeperId));
            assertThat(entidades.listar()).anyMatch(e -> e instanceof Zombie && e.getVida() == 13);
        }
    }

    private EntidadDatos datos(String nombre, double x) {
        return new EntidadDatos(nombre, 20, 5, 1.0,
                new EntidadDatos.HitboxDatos(1.0, 1.0, 1.0), List.of(), List.of(),
                TipoEntidad.desdeNombre(nombre), false, new EntidadDatos.PosicionDatos(x, 64.0, 0.0));
    }
}
