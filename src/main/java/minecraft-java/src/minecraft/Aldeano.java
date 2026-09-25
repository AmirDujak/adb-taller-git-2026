package minecraft;

import java.util.List;

public class Aldeano extends NoHostiles {

    private final String profesion;

    public Aldeano(String profesion) {
        super("Aldeano", 20, new Hitbox(0.6, 1.95, 0.6),
                List.of("murmullo", "no_no"), List.of("esmeraldas"),
                0.5);
        if (profesion == null || profesion.isBlank()) {
            throw new IllegalArgumentException("El aldeano necesita una profesión");
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
