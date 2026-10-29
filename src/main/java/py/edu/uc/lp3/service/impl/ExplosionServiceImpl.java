package py.edu.uc.lp3.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.edu.uc.lp3.domain.Creeper;
import py.edu.uc.lp3.domain.EntidadRegistro;
import py.edu.uc.lp3.domain.TipoEntidad;
import py.edu.uc.lp3.exceptions.EntidadNoEncontradaException;
import py.edu.uc.lp3.repository.EntidadRepository;
import py.edu.uc.lp3.rest.dto.ExplosionDatos;
import py.edu.uc.lp3.rest.dto.ExplosionResultado;
import py.edu.uc.lp3.rest.dto.ExplosionResultado.EstadoCreeper;
import py.edu.uc.lp3.rest.dto.ExplosionResultado.EstadoObjetivo;
import py.edu.uc.lp3.service.ExplosionService;

/** Aplica atómicamente la explosión sobre las entidades y posiciones persistidas. */
@Service
public class ExplosionServiceImpl implements ExplosionService {

    private final EntidadRepository repository;

    public ExplosionServiceImpl(EntidadRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ExplosionResultado simular(ExplosionDatos datos) {
        if (datos == null || datos.creeperId() == null || datos.creeperId() <= 0) {
            throw new IllegalArgumentException("creeperId debe ser un identificador positivo");
        }
        List<EntidadRegistro> entidades = repository.buscarParaExplosion();
        EntidadRegistro origen = entidades.stream()
                .filter(e -> e.getId().equals(datos.creeperId())).findFirst()
                .orElseThrow(() -> new EntidadNoEncontradaException(datos.creeperId()));
        if (origen.getTipo() != TipoEntidad.CREEPER) {
            throw new IllegalArgumentException("La entidad indicada no es un Creeper");
        }
        if (origen.getPosicion() == null) {
            throw new IllegalArgumentException("El Creeper necesita una posición guardada para explotar");
        }
        Creeper creeper = new Creeper();
        if (origen.isCargado()) {
            creeper.cargarConRayo();
        }
        List<EstadoObjetivo> objetivos = new ArrayList<>();
        for (EntidadRegistro objetivo : entidades) {
            if (objetivo.getId().equals(origen.getId()) || objetivo.getPosicion() == null) {
                continue;
            }
            double distancia = origen.getPosicion().distanciaA(objetivo.getPosicion());
            if (distancia > Creeper.RADIO_EXPLOSION) {
                continue;
            }
            int vidaInicial = objetivo.getVida();
            int dano = objetivo.recibirDanio(creeper.calcularDanioExplosion(distancia));
            boolean eliminado = objetivo.getVida() == 0;
            objetivos.add(new EstadoObjetivo(objetivo.getId(), objetivo.getNombre(), distancia,
                    vidaInicial, dano, objetivo.getVida(), eliminado));
            if (eliminado) {
                repository.delete(objetivo);
            }
            // Hibernate guarda por dirty checking la vida de los supervivientes al confirmar.
        }
        String mensaje = creeper.comportamiento();
        origen.recibirDanio(origen.getVida());
        repository.delete(origen);
        return new ExplosionResultado(Creeper.RADIO_EXPLOSION,
                new EstadoCreeper(origen.getId(), origen.isCargado(), 0, true, mensaje),
                List.copyOf(objetivos));
    }
}
