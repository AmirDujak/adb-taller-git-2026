package py.edu.uc.lp3.domain;

import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("CREEPER")
public class Creeper extends Hostiles {

    private Boolean cargado; // anulable para las filas anteriores a las explosiones

    public Creeper() {
        super("Creeper", 20, new Hitbox(0.6, 1.7, 0.6),
                List.of("siseo", "explosion"), List.of("polvora"),
                0.8, 16.0);
        this.cargado = false;
    }

    public boolean isCargado() {
        return Boolean.TRUE.equals(cargado);
    }

    void actualizarCarga(boolean cargado) { this.cargado = cargado; }

    public void setCargado(boolean cargado) { this.cargado = cargado; }
}
