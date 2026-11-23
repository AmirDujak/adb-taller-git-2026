package py.edu.uc.lp3.rest.dto;

/** PUT remoto: los atributos específicos se omiten para que el servidor los conserve. */
public record ActualizarSaludRemotaDatos(String nombre, String tipo, double salud,
                                        EntidadRemotaDatos.Ubicacion ubicacion) {
}
