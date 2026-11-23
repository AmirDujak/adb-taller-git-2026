package py.edu.uc.lp3.service;

import java.util.UUID;
import py.edu.uc.lp3.rest.dto.ActualizarSaludRemotaDatos;
import py.edu.uc.lp3.rest.dto.EntidadRemotaDatos;

public interface EntidadRemotaService {

    EntidadRemotaDatos buscar(UUID id);

    EntidadRemotaDatos actualizarSalud(UUID id, ActualizarSaludRemotaDatos datos);
}
