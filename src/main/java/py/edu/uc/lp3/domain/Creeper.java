package py.edu.uc.lp3.domain;

import java.util.List;

public class Creeper extends Hostiles {

    public static final double RADIO_EXPLOSION = 3.0;
    private static final int DANO_MAXIMO_EXPLOSION = 20;

    private boolean cargado; // creeper cargado por un rayo

    public Creeper() {
        super("Creeper", 20, new Hitbox(0.6, 1.7, 0.6),
                List.of("siseo", "explosion"), List.of("polvora"),
                0.8, 16.0);
        this.cargado = false;
    }

    public boolean isCargado() {
        return cargado;
    }

    public void cargarConRayo() {
        this.cargado = true;
    }

    /**
     * Daño lineal según la distancia, redondeado al entero más cercano.
     * En el borde del radio y fuera de él el daño es cero.
     * Un Creeper cargado duplica el daño; uno muerto no puede provocarlo.
     */
    public int calcularDanioExplosion(double distancia) {
        if (!Double.isFinite(distancia) || distancia < 0) {
            throw new IllegalArgumentException("La distancia debe ser finita y no negativa");
        }
        if (!isViva() || distancia >= RADIO_EXPLOSION) {
            return 0;
        }
        int maximo = DANO_MAXIMO_EXPLOSION * (cargado ? 2 : 1);
        return (int) Math.round(maximo * (1 - distancia / RADIO_EXPLOSION));
    }

    @Override
    protected String describirAvance() {
        return getNombre() + " se acerca sigilosamente, sin hacer ruido";
    }

    /** A diferencia del resto, el Creeper muere al atacar. */
    @Override
    protected String accionDeAtaque() {
        return getNombre() + " explota" + (cargado ? " ¡con carga extra!" : "")
                + ". " + desaparecer();
    }
}
