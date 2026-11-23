package py.edu.uc.lp3.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;

/** Registro de una operación autorizada, no de una explosión ejecutada. */
@Entity
@Table(name = "jugadas_cruzadas")
public class JugadaCruzada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private Instant fecha;
    @Column(nullable = false, updatable = false)
    private UUID creeperSofiaId;
    @Column(nullable = false, updatable = false)
    private UUID jugadorSofiaId;
    @Column(nullable = false, updatable = false)
    private UUID cerdoSofiaId;
    @Column(nullable = false, updatable = false)
    private UUID explosivoNicolasId;
    @Column(nullable = false, updatable = false)
    private boolean aceptadaSofia;
    @Column(nullable = false, updatable = false)
    private boolean puedeExplotarNicolas;

    protected JugadaCruzada() { }

    public JugadaCruzada(UUID creeper, UUID jugador, UUID cerdo, UUID explosivo,
            boolean aceptadaSofia, boolean puedeExplotarNicolas) {
        this.fecha = Instant.now();
        this.creeperSofiaId = creeper;
        this.jugadorSofiaId = jugador;
        this.cerdoSofiaId = cerdo;
        this.explosivoNicolasId = explosivo;
        this.aceptadaSofia = aceptadaSofia;
        this.puedeExplotarNicolas = puedeExplotarNicolas;
    }

    public Long getId() { return id; }
    public Instant getFecha() { return fecha; }
    public UUID getCreeperSofiaId() { return creeperSofiaId; }
    public UUID getJugadorSofiaId() { return jugadorSofiaId; }
    public UUID getCerdoSofiaId() { return cerdoSofiaId; }
    public UUID getExplosivoNicolasId() { return explosivoNicolasId; }
    public boolean isAceptadaSofia() { return aceptadaSofia; }
    public boolean isPuedeExplotarNicolas() { return puedeExplotarNicolas; }
}
