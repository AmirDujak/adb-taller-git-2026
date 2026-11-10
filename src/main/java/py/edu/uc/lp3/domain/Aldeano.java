package py.edu.uc.lp3.domain;

import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("ALDEANO")
public class Aldeano extends NoHostiles {

    @jakarta.persistence.Column(columnDefinition = "varchar(255) default 'Granjero'")
    private String profesion;

    protected Aldeano() {
        this("Granjero");
    }

    public Aldeano(String profesion) {
        super("Aldeano", 20, new Hitbox(0.6, 1.95, 0.6),
                List.of("murmullo", "no_no"), List.of("esmeraldas"),
                0.5);
        if (profesion == null || profesion.isBlank()) {
            throw new py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException("El aldeano necesita una profesión");
        }
        this.profesion = profesion;
    }

    public String getProfesion() {
        return profesion;
    }

    @Override
    protected String interactuar() {
        return getNombre() + " (" + profesion + ") abre su menú de comercio.";
    }
}
