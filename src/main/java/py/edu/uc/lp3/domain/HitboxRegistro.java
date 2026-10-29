package py.edu.uc.lp3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class HitboxRegistro {

    @Column(nullable = false)
    private double ancho;
    @Column(nullable = false)
    private double alto;
    @Column(nullable = false)
    private double profundidad;

    protected HitboxRegistro() {
    }

    public HitboxRegistro(double ancho, double alto, double profundidad) {
        this.ancho = ancho;
        this.alto = alto;
        this.profundidad = profundidad;
    }

    public double getAncho() { return ancho; }
    public double getAlto() { return alto; }
    public double getProfundidad() { return profundidad; }
}
