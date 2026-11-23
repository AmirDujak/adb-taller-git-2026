package py.edu.uc.lp3.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.edu.uc.lp3.domain.Entidad;
import py.edu.uc.lp3.domain.Hitbox;
import py.edu.uc.lp3.domain.Posicion;
import py.edu.uc.lp3.domain.TipoEntidad;
import py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException;
import py.edu.uc.lp3.exceptions.EntidadNoEncontradaException;
import py.edu.uc.lp3.repository.EntidadRepository;
import py.edu.uc.lp3.rest.dto.EntidadDatos;
import py.edu.uc.lp3.service.EntidadService;

@Service
@Transactional(readOnly = true)
public class EntidadServiceImpl implements EntidadService {
    private final EntidadRepository repository;

    public EntidadServiceImpl(EntidadRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Entidad> listar() {
        List<Entidad> entidades = repository.findAll();
        entidades.forEach(this::cargarColecciones);
        return entidades;
    }

    @Override
    public Entidad buscar(Long id) {
        Entidad entidad = repository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException(id));
        cargarColecciones(entidad);
        return entidad;
    }

    @Override
    @Transactional
    public Entidad crear(EntidadDatos datos) {
        return repository.save(construir(datos, null));
    }

    @Override
    @Transactional
    public Entidad actualizar(Long id, EntidadDatos datos) {
        Entidad existente = buscar(id);
        existente.actualizarDesde(construir(datos, existente));
        return existente;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Entidad entidad = buscar(id);
        entidad.retirarDelMundo();
        repository.delete(entidad);
    }

    /** Solo adapta ausencia de datos JSON; las reglas numéricas viven en el dominio. */
    private Entidad construir(EntidadDatos datos, Entidad existente) {
        if (datos == null || datos.nombre() == null || datos.vida() == null
                || datos.danoBase() == null || datos.velocidadMovimiento() == null
                || datos.hitbox() == null || datos.hitbox().ancho() == null
                || datos.hitbox().alto() == null || datos.hitbox().profundidad() == null) {
            throw new EstadoEntidadInvalidoException("Faltan campos obligatorios de la entidad");
        }
        TipoEntidad tipo = datos.tipo() != null ? datos.tipo()
                : existente != null ? existente.getTipo() : TipoEntidad.desdeNombre(datos.nombre());
        boolean cargado = datos.cargado() != null ? datos.cargado()
                : existente != null && existente.isCargado();
        Posicion posicion = existente != null ? existente.getPosicion() : null;
        if (datos.posicion() != null) {
            var p = datos.posicion();
            if (p.x() == null || p.y() == null || p.z() == null)
                throw new EstadoEntidadInvalidoException("La posición necesita x, y, z");
            posicion = new Posicion(p.x(), p.y(), p.z());
        }
        var h = datos.hitbox();
        return Entidad.crear(datos.nombre(), datos.vida(), datos.danoBase(), datos.velocidadMovimiento(),
                new Hitbox(h.ancho(), h.alto(), h.profundidad()), datos.sonidos(), datos.drop(),
                tipo, cargado, posicion);
    }

    private void cargarColecciones(Entidad entidad) {
        entidad.getSonidos();
        entidad.getDrop();
    }
}
