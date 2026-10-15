package minecraft;

public class ZombiePequeño extends Zombie {

    private final int velocidadAumentada;

    public ZombiePequeño() {
        this(10, 3, 1.2, true, 3);
    }

    public ZombiePequeño(int vida, int danoBase, double velocidad, boolean hostilidad,
                         int velocidadAumentada) {
        super(vida, danoBase, velocidad, hostilidad);
        if (velocidadAumentada < 0) {
            throw new IllegalArgumentException("La velocidad aumentada no puede ser negativa");
        }
        this.velocidadAumentada = velocidadAumentada;
    }

    public int getVelocidadAumentada() {
        return velocidadAumentada;
    }
}
