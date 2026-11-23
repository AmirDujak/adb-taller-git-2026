package py.edu.uc.lp3.exceptions;

/** Traducción del contrato HTTP remoto, con atribución explícita del proveedor. */
public class ValidacionCompaneroException extends RuntimeException {
    private final String proveedor;
    private final int estado;
    private final String codigo;

    public ValidacionCompaneroException(String proveedor, int estado, String codigo, String mensaje) {
        super(mensaje);
        this.proveedor = proveedor;
        this.estado = estado;
        this.codigo = codigo;
    }

    public String getProveedor() { return proveedor; }
    public int getEstado() { return estado; }
    public String getCodigo() { return codigo; }
}
