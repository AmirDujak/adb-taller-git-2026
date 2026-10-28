package py.edu.uc.lp3.adb.adb_taller_git_2026.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.edu.uc.lp3.adb.adb_taller_git_2026.dto.EntidadDatos;
import py.edu.uc.lp3.adb.adb_taller_git_2026.model.EntidadRegistro;
import py.edu.uc.lp3.adb.adb_taller_git_2026.model.HitboxRegistro;
import py.edu.uc.lp3.adb.adb_taller_git_2026.repository.EntidadRepository;

@Service
@Transactional(readOnly = true)
public class EntidadService {

    private final EntidadRepository repository;

    public EntidadService(EntidadRepository repository) {
        this.repository = repository;
    }

    public List<EntidadRegistro> listar() {
        List<EntidadRegistro> entidades = repository.findAll();
        entidades.forEach(this::cargarColecciones);
        return entidades;
    }

    public EntidadRegistro buscar(Long id) {
        EntidadRegistro entidad = repository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException(id));
        cargarColecciones(entidad);
        return entidad;
    }

    @Transactional
    public EntidadRegistro crear(EntidadDatos datos) {
        return repository.save(validarYConstruir(datos));
    }

    @Transactional
    public EntidadRegistro actualizar(Long id, EntidadDatos datos) {
        EntidadRegistro existente = buscar(id);
        existente.actualizarDesde(validarYConstruir(datos));
        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private EntidadRegistro validarYConstruir(EntidadDatos datos) {
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
        return new EntidadRegistro(datos.nombre().strip(), datos.vida(), datos.danoBase(),
                datos.velocidadMovimiento(),
                new HitboxRegistro(hitbox.ancho(), hitbox.alto(), hitbox.profundidad()),
                datos.sonidos(), datos.drop());
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
