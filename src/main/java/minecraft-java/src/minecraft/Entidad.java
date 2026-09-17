package minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase base "Entidades" del diagrama.
 * Toda entidad del juego (hostil o no hostil) hereda de aquí.
 */
public abstract class Entidad implements Movimientos {

    private int vida;
    private final String nombre;
    private final List<String> sonidos;
    private final Hitbox hitbox;
    private double velocidadMovimiento;
    private final List<String> drop;

    protected Entidad(String nombre, int vida, Hitbox hitbox,
                       List<String> sonidos, List<String> drop,
                       double velocidadMovimiento) {
        this.nombre = nombre;
        this.vida = vida;
        this.hitbox = hitbox;
        this.sonidos = new ArrayList<>(sonidos);
        this.drop = new ArrayList<>(drop);
        this.velocidadMovimiento = velocidadMovimiento;
    }

    // ---- Getters ----

    public int getVida() {
        return vida;
    }

    public String getNombre() {
        return nombre;
    }

    public List<String> getSonidos() {
        return sonidos;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public double getVelocidadMovimiento() {
        return velocidadMovimiento;
    }

    public List<String> getDrop() {
        return drop;
    }

    // ---- Comportamiento común ----

    public void recibirDanio(int cantidad) {
        vida = Math.max(0, vida - cantidad);
        emitirSonido();
        if (!estaVivo()) {
            morir();
        }
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    /** Cuando la entidad muere, suelta su drop. */
    protected void morir() {
        System.out.println(nombre + " ha muerto y suelta: " + drop);
    }

    private void emitirSonido() {
        if (!sonidos.isEmpty()) {
            System.out.println(nombre + " hace el sonido: " + sonidos.get(0));
        }
    }

    // ---- Implementación por defecto de Movimientos ----
    // Las subclases pueden sobrescribir estos métodos si necesitan un
    // comportamiento distinto (por ejemplo, un Creeper que "avanza" hacia el jugador).

    @Override
    public void avanzar() {
        System.out.println(nombre + " avanza a velocidad " + velocidadMovimiento);
    }

    @Override
    public void rotar(double angulo) {
        System.out.println(nombre + " rota " + angulo + " grados");
    }

    @Override
    public void saltar() {
        System.out.println(nombre + " salta");
    }

    @Override
    public String toString() {
        return String.format("%s [vida=%d, hitbox=%s, drop=%s]",
                nombre, vida, hitbox, drop);
    }
}
