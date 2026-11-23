package py.edu.uc.lp3.exceptions;

/** Error al consultar el proveedor externo, independiente de las reglas del juego. */
public class ConsultaObjetosException extends RuntimeException {

    public ConsultaObjetosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
