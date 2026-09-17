package minecraft;

/**
 * Representa el cuerpo/colisión física de una entidad (lo que en el
 * diagrama aparece como "-Hitbox // fisico").
 */
public class Hitbox {

    private double ancho;
    private double alto;
    private double profundidad;

    public Hitbox(double ancho, double alto, double profundidad) {
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

    /**
     * Colisión simple tipo AABB (ejemplo básico, se puede refinar luego).
     */
    public boolean colisionaCon(Hitbox otro) {
        return this.ancho + otro.ancho > 0 && this.alto + otro.alto > 0;
    }

    @Override
    public String toString() {
        return String.format("Hitbox[%.2f x %.2f x %.2f]", ancho, alto, profundidad);
    }
}
