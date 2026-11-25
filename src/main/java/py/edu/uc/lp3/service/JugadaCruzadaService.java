package py.edu.uc.lp3.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import py.edu.uc.lp3.domain.JugadaCruzada;
import py.edu.uc.lp3.exceptions.EntidadNoEncontradaException;
import py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException;
import py.edu.uc.lp3.repository.JugadaCruzadaRepository;
import py.edu.uc.lp3.rest.dto.JugadaCruzadaDatos;

@Service
public class JugadaCruzadaService {
    private final SofiaValidacionClient sofia;
    private final NicolasValidacionClient nicolas;
    private final JugadaCruzadaRepository repository;
    private final TransactionTemplate escritura;

    public JugadaCruzadaService(SofiaValidacionClient sofia, NicolasValidacionClient nicolas,
            JugadaCruzadaRepository repository, PlatformTransactionManager transactions) {
        this.sofia = sofia;
        this.nicolas = nicolas;
        this.repository = repository;
        this.escritura = new TransactionTemplate(transactions);
    }

    public JugadaCruzada ejecutar(JugadaCruzadaDatos datos) {
        if (datos == null || datos.creeperSofiaId() == null || datos.jugadorSofiaId() == null
                || datos.cerdoSofiaId() == null || datos.explosivoNicolasId() == null) {
            throw new EstadoEntidadInvalidoException("Se requieren los cuatro UUID de los participantes");
        }
        // Los tipos, IDs distintos y estados son reglas de los proveedores, no se copian aquí.
        sofia.validar(datos);
        nicolas.validar(datos.explosivoNicolasId());
        // No hay transacción de escritura ni cambios locales mientras se espera la red.
        return escritura.execute(status -> repository.saveAndFlush(new JugadaCruzada(
                datos.creeperSofiaId(), datos.jugadorSofiaId(), datos.cerdoSofiaId(),
                datos.explosivoNicolasId(), true, true)));
    }

    public List<JugadaCruzada> listar() {
        return repository.findAll(Sort.by("id"));
    }

    public JugadaCruzada buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new EntidadNoEncontradaException("No existe la jugada cruzada " + id));
    }
}
