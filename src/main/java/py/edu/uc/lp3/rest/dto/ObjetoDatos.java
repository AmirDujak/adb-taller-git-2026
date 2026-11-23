package py.edu.uc.lp3.rest.dto;

import java.util.Map;

/** data contiene atributos variables del proveedor y puede ser null. */
public record ObjetoDatos(String id, String name, Map<String, Object> data) {
}
