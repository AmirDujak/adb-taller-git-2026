package py.edu.uc.lp3.service;

import java.util.List;
import py.edu.uc.lp3.domain.*;
import org.junit.jupiter.api.Test;
import py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException;
import py.edu.uc.lp3.exceptions.ExplosionNoPermitidaException;
import static org.assertj.core.api.Assertions.*;

class EntidadReglasTests {
    private final ReglasJuegoService reglas = new ReglasJuegoService();
    @Test
    void servicioValidaElEstadoAntesDeUsarlo() {
        assertThatThrownBy(() -> reglas.validarInicial(new Player(0, 5, 1)))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.validarInicial(new Zombie(20, -1, 1, true)))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.validarInicial(new Player(20, 5, Double.NaN)))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.crear("Player", 20, 5, 1, new Hitbox(1, Double.POSITIVE_INFINITY, 1),
                List.of(), List.of(), TipoEntidad.PLAYER, false, null))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.crear("Zombie", 20, 5, 1, new Hitbox(1, 1, 1),
                List.of(), List.of(), TipoEntidad.ZOMBIE, true, null))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
    }

    @Test
    void laExcepcionDeExplosionLaLanzaElService() {
        assertThatThrownBy(() -> reglas.prepararExplosion(new Player()))
                .isInstanceOf(ExplosionNoPermitidaException.class);
        assertThatThrownBy(() -> reglas.prepararExplosion(new Creeper()))
                .isInstanceOf(ExplosionNoPermitidaException.class);
    }

    @Test
    void listasNoExponenMutacionYDanioNoDejaVidaNegativa() {
        Player player = new Player();
        assertThatThrownBy(() -> player.getDrop().add("diamante"))
                .isInstanceOf(UnsupportedOperationException.class);
        reglas.recibirDanio(player, Integer.MAX_VALUE);
        assertThat(player.getVida()).isZero();
        assertThat(reglas.movimientos(player).avanzar()).contains("no puede moverse");
    }

    @Test
    void validaProfesionPosicionYVelocidadAumentadaDesdeService() {
        assertThatThrownBy(() -> reglas.validarInicial(new Aldeano(" ")))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.validarInicial(new ZombiePequeño(10, 3, 1, true, -1)))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.validarPosicion(new Posicion(Double.NaN, 0, 0)))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.recibirDanio(new Player(), -1))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
        assertThatThrownBy(() -> reglas.detecta(new Zombie(), -1))
                .isInstanceOf(EstadoEntidadInvalidoException.class);
    }
}
