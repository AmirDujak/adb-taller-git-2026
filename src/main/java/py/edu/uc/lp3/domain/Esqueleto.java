package py.edu.uc.lp3.domain;

import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("ESQUELETO")
public class Esqueleto extends Hostiles {

    @jakarta.persistence.Column(columnDefinition = "boolean default true")
    private boolean usaArco;

    /** Esqueleto por defecto: usa arco, vida 20, daño base 3, velocidad 0.75 y rango de aggro 14.0. */
    public Esqueleto() {
        this(20, 3, 0.75, true);
    }

    /**
     * Esqueleto con parámetros personalizados.
     *
     * @param vida     puntos de vida iniciales
     * @param danoBase daño base de su ataque
     * @param velocidad velocidad de movimiento
     * @param usaArco  si dispara con arco o ataca cuerpo a cuerpo
     */
    public Esqueleto(int vida, int danoBase, double velocidad, boolean usaArco) {
        super("Esqueleto", vida, new Hitbox(0.6, 1.99, 0.6),
                List.of("hueso", "disparo_arco"), List.of("huesos", "flechas"),
                velocidad, danoBase, 14.0);
        this.usaArco = usaArco;
    }

    public boolean isUsaArco() {
        return usaArco;
    }

}
