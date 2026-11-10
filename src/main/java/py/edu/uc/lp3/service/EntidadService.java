package py.edu.uc.lp3.service;

import java.util.List;

import py.edu.uc.lp3.domain.Entidad;
import py.edu.uc.lp3.rest.dto.EntidadDatos;

/** Contrato de las operaciones de negocio del CRUD de entidades. */
public interface EntidadService {

    List<Entidad> listar();

    Entidad buscar(Long id);

    Entidad crear(EntidadDatos datos);

    Entidad actualizar(Long id, EntidadDatos datos);

    void eliminar(Long id);
}
