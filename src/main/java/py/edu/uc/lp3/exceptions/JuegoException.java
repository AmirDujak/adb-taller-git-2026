package py.edu.uc.lp3.exceptions;

/** Fallo no comprobado del dominio; provoca rollback de la transacción. */
public class JuegoException extends RuntimeException {
    public JuegoException(String mensaje) {
        super(mensaje);
    }
}
