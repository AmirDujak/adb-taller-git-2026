package py.edu.uc.lp3.adb.adb_taller_git_2026.service;

public class EntidadNoEncontradaException extends RuntimeException {
    public EntidadNoEncontradaException(Long id) {
        super("No existe la entidad con id " + id);
    }
}
