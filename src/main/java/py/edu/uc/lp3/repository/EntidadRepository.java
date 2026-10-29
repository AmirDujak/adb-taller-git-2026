package py.edu.uc.lp3.repository;

import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import py.edu.uc.lp3.domain.EntidadRegistro;

public interface EntidadRepository extends JpaRepository<EntidadRegistro, Long> {

    /** Serializa las explosiones para no aplicar dos veces el mismo estado inicial. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EntidadRegistro e order by e.id")
    List<EntidadRegistro> buscarParaExplosion();
}
