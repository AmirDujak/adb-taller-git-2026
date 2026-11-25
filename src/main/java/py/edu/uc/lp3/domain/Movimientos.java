package py.edu.uc.lp3.domain;

/**
 * Contrato de descripciones de movimiento, implementado por un adaptador en service.
 * Corresponde al bloque "Movimientos" del diagrama (Avanzar, Rotar, Saltar).
 * Cada método devuelve la descripción de lo que ocurrió.
 */
public interface Movimientos {

    String avanzar();

    String rotar(double angulo);

    String saltar();
}
