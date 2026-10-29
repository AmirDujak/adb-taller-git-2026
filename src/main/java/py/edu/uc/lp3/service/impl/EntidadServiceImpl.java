package py.edu.uc.lp3.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.edu.uc.lp3.rest.dto.EntidadDatos;
import py.edu.uc.lp3.domain.EntidadRegistro;
import py.edu.uc.lp3.domain.HitboxRegistro;
import py.edu.uc.lp3.domain.Posicion;
import py.edu.uc.lp3.domain.TipoEntidad;
import py.edu.uc.lp3.repository.EntidadRepository;
import py.edu.uc.lp3.exceptions.EntidadNoEncontradaException;
import py.edu.uc.lp3.service.EntidadService;

@Service
@Transactional(readOnly = true)
public class EntidadServiceImpl implements EntidadService {

    private final EntidadRepository repository;

    public EntidadServiceImpl(EntidadRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EntidadRegistro> listar() {
        List<EntidadRegistro> entidades = repository.findAll();
        entidades.forEach(this::cargarColecciones);
        return entidades;
    }

    @Override
    public EntidadRegistro buscar(Long id) {
        EntidadRegistro entidad = repository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException(id));
        cargarColecciones(entidad);
        return entidad;
    }

    @Override
    @Transactional
    public EntidadRegistro crear(EntidadDatos datos) {
        return repository.save(validarYConstruir(datos, null));
    }

    @Override
    @Transactional
    public EntidadRegistro actualizar(Long id, EntidadDatos datos) {
        EntidadRegistro existente = buscar(id);
        existente.actualizarDesde(validarYConstruir(datos, existente));
        return repository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private EntidadRegistro validarYConstruir(EntidadDatos datos, EntidadRegistro existente) {
        if (datos == null || datos.nombre() == null || datos.nombre().isBlank()
                || datos.nombre().strip().length() > 255) {
            throw new IllegalArgumentException("El nombre es obligatorio y debe tener hasta 255 caracteres");
        }
        if (datos.vida() == null || datos.vida() <= 0) {
            throw new IllegalArgumentException("La vida debe ser positiva");
        }
        if (datos.danoBase() == null || datos.danoBase() < 0) {
            throw new IllegalArgumentException("El daño base debe ser cero o positivo");
        }
        if (datos.velocidadMovimiento() == null || !Double.isFinite(datos.velocidadMovimiento())
                || datos.velocidadMovimiento() < 0) {
            throw new IllegalArgumentException("La velocidad debe ser finita y no negativa");
        }
        var hitbox = datos.hitbox();
        if (hitbox == null || !esDimensionValida(hitbox.ancho())
                || !esDimensionValida(hitbox.alto()) || !esDimensionValida(hitbox.profundidad())) {
            throw new IllegalArgumentException("La hitbox es obligatoria y sus dimensiones deben ser finitas y positivas");
        }
        validarLista(datos.sonidos(), "sonidos");
        validarLista(datos.drop(), "drop");
        TipoEntidad tipo = datos.tipo() != null ? datos.tipo()
                : existente != null ? existente.getTipo() : TipoEntidad.desdeNombre(datos.nombre());
        boolean cargado = datos.cargado() != null ? datos.cargado()
                : existente != null && existente.isCargado();
        if (cargado && tipo != TipoEntidad.CREEPER) {
            throw new IllegalArgumentException("Solo un Creeper puede estar cargado");
        }
        Posicion posicion = existente != null ? existente.getPosicion() : null;
        if (datos.posicion() != null) {
            var coordenadas = datos.posicion();
            if (coordenadas.x() == null || coordenadas.y() == null || coordenadas.z() == null) {
                throw new IllegalArgumentException("La posición necesita x, y, z");
            }
            posicion = new Posicion(coordenadas.x(), coordenadas.y(), coordenadas.z());
        }
        return new EntidadRegistro(datos.nombre().strip(), datos.vida(), datos.danoBase(),
                datos.velocidadMovimiento(),
                new HitboxRegistro(hitbox.ancho(), hitbox.alto(), hitbox.profundidad()),
                datos.sonidos(), datos.drop(), tipo, cargado, posicion);
    }

    private boolean esDimensionValida(Double valor) {
        return valor != null && Double.isFinite(valor) && valor > 0;
    }

    private void validarLista(List<String> valores, String campo) {
        if (valores == null || valores.stream().anyMatch(
                valor -> valor == null || valor.isBlank() || valor.length() > 255)) {
            throw new IllegalArgumentException("La lista " + campo
                    + " es obligatoria; sus elementos deben tener entre 1 y 255 caracteres");
        }
    }

    // Inicializa las listas dentro de la transacción, sin depender de Open Session in View.
    private void cargarColecciones(EntidadRegistro entidad) {
        entidad.getSonidos();
        entidad.getDrop();
    }
}
