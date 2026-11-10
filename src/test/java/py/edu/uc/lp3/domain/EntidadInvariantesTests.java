package py.edu.uc.lp3.domain;

import java.util.List;
import org.junit.jupiter.api.Test;
import py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException;
import py.edu.uc.lp3.exceptions.ExplosionNoPermitidaException;
import static org.assertj.core.api.Assertions.*;

class EntidadInvariantesTests {
    @Test
    void constructoresSostienenReglasSinControllerNiService() {
        assertThatThrownBy(() -> new Player(0, 5, 1))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> new Zombie(20, -1, 1, true))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> new Player(20, 5, Double.NaN))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> new Hitbox(1, Double.POSITIVE_INFINITY, 1))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> Entidad.crear("Zombie", 20, 5, 1, new Hitbox(1, 1, 1),
                List.of(), List.of(), TipoEntidad.ZOMBIE, true, null))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
    }

    @Test
    void laExcepcionDeExplosionLaLanzaElObjetoDelJuego() {
        assertThatThrownBy(() -> new Player().prepararExplosion())
                .isInstanceOf(ExplosionNoPermitidaException.class);
        assertThatThrownBy(() -> new Creeper().prepararExplosion())
                .isInstanceOf(ExplosionNoPermitidaException.class);
    }

    @Test
    void listasNoExponenMutacionYDanioNoDejaVidaNegativa() {
        Player player = new Player();
        assertThatThrownBy(() -> player.getDrop().add("diamante"))
                .isInstanceOf(UnsupportedOperationException.class);
        player.recibirDanio(Integer.MAX_VALUE);
        assertThat(player.getVida()).isZero();
        assertThat(player.avanzar()).contains("no puede moverse");
    }
}
