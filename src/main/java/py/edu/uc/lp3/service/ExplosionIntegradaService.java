package py.edu.uc.lp3.service;

import py.edu.uc.lp3.rest.dto.ExplosionIntegradaDatos;
import py.edu.uc.lp3.rest.dto.ExplosionIntegradaResultado;
import py.edu.uc.lp3.rest.dto.ExplosionAplicadaResultado;

public interface ExplosionIntegradaService {

    /** Calcula una estimación con datos locales y remotos sin modificar ninguno de ellos. */
    ExplosionIntegradaResultado previsualizar(ExplosionIntegradaDatos datos);

    /** Aplica daño remoto con PUT independientes; no consume al Creeper local. */
    ExplosionAplicadaResultado aplicar(ExplosionIntegradaDatos datos);
}
