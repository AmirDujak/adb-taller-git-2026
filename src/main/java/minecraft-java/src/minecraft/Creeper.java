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

    @Override
    public void accionDeAtaque() {
        System.out.println(getNombre() + " se acerca sigilosamente y explota"
                + (cargado ? " ¡con carga extra!" : "."));
    }

    public void cargarConRayo() {
        this.cargado = true;
    }
}
