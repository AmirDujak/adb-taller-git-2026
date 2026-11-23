package py.edu.uc.lp3.service;

import java.util.List;
import py.edu.uc.lp3.rest.dto.ObjetoDatos;

public interface ObjetoService {

    /** Consulta los objetos de la API externa sin persistirlos. */
    List<ObjetoDatos> listar();
}
