package py.edu.uc.lp3.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CreeperTests {

    @Test
    void danioDisminuyeConLaDistanciaYSeDuplicaConCarga() {
        Creeper creeper = new Creeper();
        assertThat(creeper.calcularDanioExplosion(0)).isEqualTo(20);
        assertThat(creeper.calcularDanioExplosion(1)).isEqualTo(13);
        assertThat(creeper.calcularDanioExplosion(1.5)).isEqualTo(10);
        assertThat(creeper.calcularDanioExplosion(3)).isZero();
        assertThat(creeper.calcularDanioExplosion(4)).isZero();
        creeper.cargarConRayo();
        assertThat(creeper.calcularDanioExplosion(0)).isEqualTo(40);
        assertThat(creeper.calcularDanioExplosion(1.5)).isEqualTo(20);
    }

    @Test
    void creeperMuertoNoPuedeVolverAExplotar() {
        Creeper creeper = new Creeper();
        assertThat(creeper.comportamiento()).contains("explota");
        assertThat(creeper.isViva()).isFalse();
        assertThat(creeper.calcularDanioExplosion(0)).isZero();
        assertThat(creeper.comportamiento()).doesNotContain("explota");
    }

    @Test
    void entidadMuertaNoRecibeMasDanio() {
        Zombie zombie = new Zombie();
        zombie.recibirDanio(40);
        assertThat(zombie.getVida()).isZero();
        assertThat(zombie.recibirDanio(10)).contains("no puede recibir daño");
        assertThat(zombie.getVida()).isZero();
    }

    @Test
    void rechazaDistanciasInvalidas() {
        Creeper creeper = new Creeper();
        for (double distancia : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThatIllegalArgumentException().isThrownBy(() -> creeper.calcularDanioExplosion(distancia));
        }
    }
}
