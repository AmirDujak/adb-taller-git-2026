package py.edu.uc.lp3.service;

import py.edu.uc.lp3.domain.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExplosionReglasTests {
    private final ReglasJuegoService reglas = new ReglasJuegoService();

    @Test
    void danioDisminuyeConLaDistanciaYSeDuplicaConCarga() {
        Creeper creeper = new Creeper();
        assertThat(reglas.calcularDanioExplosion(creeper, 0)).isEqualTo(20);
        assertThat(reglas.calcularDanioExplosion(creeper, 1)).isEqualTo(13);
        assertThat(reglas.calcularDanioExplosion(creeper, 1.5)).isEqualTo(10);
        assertThat(reglas.calcularDanioExplosion(creeper, 3)).isZero();
        assertThat(reglas.calcularDanioExplosion(creeper, 4)).isZero();
        creeper.setCargado(true);
        assertThat(reglas.calcularDanioExplosion(creeper, 0)).isEqualTo(40);
        assertThat(reglas.calcularDanioExplosion(creeper, 1.5)).isEqualTo(20);
    }

    @Test
    void creeperMuertoNoPuedeVolverAExplotar() {
        Creeper creeper = new Creeper();
        assertThat(reglas.comportamiento(creeper)).contains("explota");
        assertThat(creeper.isViva()).isFalse();
        assertThat(reglas.calcularDanioExplosion(creeper, 0)).isZero();
        assertThat(reglas.comportamiento(creeper)).doesNotContain("explota");
    }

    @Test
    void entidadMuertaNoRecibeMasDanio() {
        Zombie zombie = new Zombie();
        reglas.recibirDanio(zombie, 40);
        assertThat(zombie.getVida()).isZero();
        assertThat(reglas.recibirDanio(zombie, 10)).contains("no puede recibir daño");
        assertThat(zombie.getVida()).isZero();
    }

    @Test
    void rechazaDistanciasInvalidas() {
        Creeper creeper = new Creeper();
        for (double distancia : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThatThrownBy(() -> reglas.calcularDanioExplosion(creeper, distancia))
                    .isInstanceOf(py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException.class);
        }
    }
}
