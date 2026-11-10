package py.edu.uc.lp3.domain;

/**
 * Representa el cuerpo/colisión física de una entidad (lo que en el
 * diagrama aparece como "-Hitbox // fisico").
 * Es inmutable: una vez creada no se puede deformar desde afuera.
 */
@jakarta.persistence.Embeddable
public class Hitbox {

    @jakarta.persistence.Column(nullable = false)
    private double ancho;
    @jakarta.persistence.Column(nullable = false)
    private double alto;
    @jakarta.persistence.Column(nullable = false)
    private double profundidad;

    protected Hitbox() {
    }

    public Hitbox(double ancho, double alto, double profundidad) {
        if (!Double.isFinite(ancho) || !Double.isFinite(alto) || !Double.isFinite(profundidad)
                || ancho <= 0 || alto <= 0 || profundidad <= 0) {
            throw new py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException("Las dimensiones de la hitbox deben ser finitas y positivas");
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
