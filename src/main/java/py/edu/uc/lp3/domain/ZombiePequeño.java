package py.edu.uc.lp3.domain;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("ZOMBIE_PEQUENO")
public class ZombiePequeño extends Zombie {

    @jakarta.persistence.Column(columnDefinition = "integer default 3")
    private int velocidadAumentada;

    public ZombiePequeño() {
        this(10, 3, 1.2, true, 3);
    }

    public ZombiePequeño(int vida, int danoBase, double velocidad, boolean hostilidad,
                         int velocidadAumentada) {
        super(vida, danoBase, velocidad, hostilidad);
        if (velocidadAumentada < 0) {
            throw new py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException("La velocidad aumentada no puede ser negativa");
        }
        this.velocidadAumentada = velocidadAumentada;
    }

    public int getVelocidadAumentada() {
        return velocidadAumentada;
    }

    @Override
    public TipoEntidad getTipo() {
        return TipoEntidad.ZOMBIE_PEQUENO;
    }
}
