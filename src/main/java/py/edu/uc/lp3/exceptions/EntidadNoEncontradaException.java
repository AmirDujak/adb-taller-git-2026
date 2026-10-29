package py.edu.uc.lp3.exceptions;

public class EntidadNoEncontradaException extends RuntimeException {
    public EntidadNoEncontradaException(Long id) {
        super("No existe la entidad con id " + id);
    }
}
