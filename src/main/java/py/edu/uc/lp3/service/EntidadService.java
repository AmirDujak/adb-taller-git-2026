package py.edu.uc.lp3.service;

import java.util.List;

import py.edu.uc.lp3.domain.EntidadRegistro;
import py.edu.uc.lp3.rest.dto.EntidadDatos;

/** Contrato de las operaciones de negocio del CRUD de entidades. */
public interface EntidadService {

    List<EntidadRegistro> listar();

    EntidadRegistro buscar(Long id);

    EntidadRegistro crear(EntidadDatos datos);

    EntidadRegistro actualizar(Long id, EntidadDatos datos);

    void eliminar(Long id);
}
