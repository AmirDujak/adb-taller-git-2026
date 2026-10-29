package py.edu.uc.lp3.service;

import py.edu.uc.lp3.rest.dto.ExplosionDatos;
import py.edu.uc.lp3.rest.dto.ExplosionResultado;

public interface ExplosionService {

    /** Aplica la explosión en H2: actualiza supervivientes y elimina muertos y emisor. */
    ExplosionResultado simular(ExplosionDatos datos);
}
