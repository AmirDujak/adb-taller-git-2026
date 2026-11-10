package py.edu.uc.lp3.repository;

import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import py.edu.uc.lp3.domain.Entidad;

public interface EntidadRepository extends JpaRepository<Entidad, Long> {

    /** Serializa las explosiones para no aplicar dos veces el mismo estado inicial. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Entidad e order by e.id")
    List<Entidad> buscarParaExplosion();
}
