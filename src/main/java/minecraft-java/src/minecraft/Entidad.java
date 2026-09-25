package minecraft;

import java.util.List;
import java.util.Objects;

/**
 * Clase base "Entidades" del diagrama.
 * Toda entidad del juego (hostil o no hostil) hereda de aquí.
 *
 * Concentra las reglas comunes de vida, movimiento y muerte. Ningún campo
 * es accesible desde afuera: la vida solo cambia a través de
 * {@link #recibirDanio(int)} o {@link #desaparecer()}, así que no es
 * posible dejar una entidad en un estado imposible (vida negativa,
 * una entidad muerta que sigue moviéndose, etc.).
 */
public abstract class Entidad implements Movimientos {

    private int vida;
    private final String nombre;
    private final List<String> sonidos;
    private final Hitbox hitbox;
    private final double velocidadMovimiento;
    private final List<String> drop;

    protected Entidad(String nombre, int vida, Hitbox hitbox,
                       List<String> sonidos, List<String> drop,
                       double velocidadMovimiento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La entidad necesita un nombre");
        }
        if (vida <= 0) {
            throw new IllegalArgumentException("La vida inicial debe ser positiva");
        }
        if (velocidadMovimiento < 0) {
            throw new IllegalArgumentException("La velocidad no puede ser negativa");
        }
        this.nombre = nombre;
        this.vida = vida;
        this.hitbox = Objects.requireNonNull(hitbox, "hitbox");
        this.sonidos = List.copyOf(sonidos);
        this.drop = List.copyOf(drop);
        this.velocidadMovimiento = velocidadMovimiento;
    }

    // ---- Getters (las listas son inmutables) ----

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

    public boolean isViva() {
        return vida > 0;
    }

    // ---- Comportamiento propio de cada entidad ----

    /**
     * Lo que hace la entidad cuando el jugador está cerca: cada rama de la
     * jerarquía lo define (un Creeper explota, un Aldeano comercia...).
     */
    public abstract String comportamiento();

    // ---- Reglas comunes de vida y muerte ----

    public final String recibirDanio(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo");
        }
        if (!isViva()) {
            return nombre + " ya desapareció, no puede recibir daño.";
        }
        vida = Math.max(0, vida - cantidad);
        String resultado = nombre + " recibe " + cantidad + " de daño"
                + (sonidos.isEmpty() ? "" : " y hace el sonido: " + sonidos.get(0));
        if (!isViva()) {
            resultado += ". " + desaparecer();
        }
        return resultado;
    }

    /**
     * Mata a la entidad y suelta su drop. Es protected porque solo la
     * jerarquía decide cuándo una entidad desaparece sin recibir daño
     * (por ejemplo, un Creeper al explotar).
     */
    protected final String desaparecer() {
        vida = 0;
        return nombre + " ha desaparecido y suelta: " + drop;
    }

    // ---- Implementación por defecto de Movimientos ----
    // Una entidad muerta no puede moverse. Las subclases solo redefinen
    // la parte que realmente cambia a través de los métodos "describir...".

    @Override
    public final String avanzar() {
        return isViva() ? describirAvance() : noPuedeMoverse();
    }

    @Override
    public final String rotar(double angulo) {
        return isViva() ? nombre + " rota " + angulo + " grados" : noPuedeMoverse();
    }

    @Override
    public final String saltar() {
        return isViva() ? nombre + " salta" : noPuedeMoverse();
    }

    /** Punto de extensión: cómo avanza esta entidad en particular. */
    protected String describirAvance() {
        return nombre + " avanza a velocidad " + velocidadMovimiento;
    }

    private String noPuedeMoverse() {
        return nombre + " ya desapareció, no puede moverse.";
    }

    @Override
    public String toString() {
        return String.format("%s [vida=%d, hitbox=%s, drop=%s]",
                nombre, vida, hitbox, drop);
    }
}
