package minecraft;

/**
 * Contrato de movimientos que toda entidad del juego puede realizar.
 * Corresponde al bloque "Movimientos" del diagrama (Avanzar, Rotar, Saltar).
 */
public interface Movimientos {

    void avanzar();

    void rotar(double angulo);

    void saltar();
}
