package minecraft;

/**
 * Contrato de movimientos que toda entidad del juego puede realizar.
 * Corresponde al bloque "Movimientos" del diagrama (Avanzar, Rotar, Saltar).
 * Cada método devuelve la descripción de lo que ocurrió.
 */
public interface Movimientos {

    String avanzar();

    String rotar(double angulo);

    String saltar();
}
