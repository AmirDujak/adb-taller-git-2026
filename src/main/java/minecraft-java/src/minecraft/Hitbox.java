package minecraft;

/**
 * Representa el cuerpo/colisión física de una entidad (lo que en el
 * diagrama aparece como "-Hitbox // fisico").
 * Es inmutable: una vez creada no se puede deformar desde afuera.
 */
public final class Hitbox {

    private final double ancho;
    private final double alto;
    private final double profundidad;

    public Hitbox(double ancho, double alto, double profundidad) {
        if (ancho <= 0 || alto <= 0 || profundidad <= 0) {
            throw new IllegalArgumentException("Las dimensiones de la hitbox deben ser positivas");
        }
        this.ancho = ancho;
        this.alto = alto;
        this.profundidad = profundidad;
    }

    public double getAncho() {
        return ancho;
    }

    public double getAlto() {
        return alto;
    }

    public double getProfundidad() {
        return profundidad;
    }

    @Override
    public String toString() {
        return String.format("Hitbox[%.2f x %.2f x %.2f]", ancho, alto, profundidad);
    }
}
