package minecraft;

import java.util.List;

public class Creeper extends Hostiles {

    private boolean cargado; // creeper cargado por un rayo

    public Creeper() {
        super("Creeper", 20, new Hitbox(0.6, 1.7, 0.6),
                List.of("siseo", "explosion"), List.of("polvora"),
                0.8, 16.0);
        this.cargado = false;
    }

    public boolean isCargado() {
        return cargado;
    }

    public void cargarConRayo() {
        this.cargado = true;
    }

    @Override
    protected String describirAvance() {
        return getNombre() + " se acerca sigilosamente, sin hacer ruido";
    }

    /** A diferencia del resto, el Creeper muere al atacar. */
    @Override
    protected String accionDeAtaque() {
        return getNombre() + " explota" + (cargado ? " ¡con carga extra!" : "")
                + ". " + desaparecer();
    }
}
