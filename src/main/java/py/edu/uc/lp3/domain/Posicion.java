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
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException("Las coordenadas deben ser finitas");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double distanciaA(Posicion otra) {
        return Math.hypot(Math.hypot(x - otra.x, y - otra.y), z - otra.z);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
