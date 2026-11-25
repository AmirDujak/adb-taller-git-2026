package py.edu.uc.lp3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Ubicación en bloques; las entidades antiguas no tienen una ubicación asumida. */
@Embeddable
public class Posicion {

    @Column(name = "posicion_x")
    private double x;
    @Column(name = "posicion_y")
    private double y;
    @Column(name = "posicion_z")
    private double z;

    protected Posicion() {
    }

    public Posicion(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
