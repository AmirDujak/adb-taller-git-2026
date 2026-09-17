package minecraft;

import java.util.List;

public class Aldeano extends NoHostiles {

    private String profesion;

    public Aldeano(String profesion) {
        super("Aldeano", 20, new Hitbox(0.6, 1.95, 0.6),
                List.of("murmullo", "no_no"), List.of("esmeraldas"),
                0.5);
        this.profesion = profesion;
    }

    public String getProfesion() {
        return profesion;
    }

    @Override
    public void interactuar() {
        System.out.println(getNombre() + " (" + profesion + ") abre su menú de comercio.");
    }
}
