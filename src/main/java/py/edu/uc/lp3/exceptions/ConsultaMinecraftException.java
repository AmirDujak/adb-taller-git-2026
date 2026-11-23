package py.edu.uc.lp3.exceptions;

/** Fallo del proveedor remoto, separado de las reglas del dominio local. */
public class ConsultaMinecraftException extends RuntimeException {

    public enum Motivo { NO_ENCONTRADA, NO_DISPONIBLE, TIEMPO_AGOTADO }

    private final Motivo motivo;

    public ConsultaMinecraftException(Motivo motivo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
