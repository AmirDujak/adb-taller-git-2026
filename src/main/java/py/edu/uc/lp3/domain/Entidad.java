package py.edu.uc.lp3.domain;

import java.util.List;
import java.util.ArrayList;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.Check;

/**
 * Clase base "Entidades" del diagrama.
 * Toda entidad del juego (hostil o no hostil) hereda de aquí.
 *
 * Modelo de estado persistido. Las validaciones, reglas y excepciones de negocio
 * pertenecen exclusivamente a los servicios (corrección POO-08).
 */
@Entity
@Schema(description = "Entidad del mundo; tipo identifica el subtipo concreto. Los campos adicionales dependen del subtipo.",
        subTypes = {Creeper.class, Player.class, Zombie.class, Esqueleto.class, Lobo.class,
                Oveja.class, Aldeano.class, Cerdo.class, ZombiePequeño.class, OtraEntidad.class})
@Table(name = "entidades")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Check(constraints = "vida >= 0 and dano_base >= 0 and velocidad_movimiento >= 0 "
        + "and ancho > 0 and alto > 0 and profundidad > 0")
@DiscriminatorFormula("coalesce(tipo, case upper(trim(nombre)) when 'CREEPER' then 'CREEPER' "
        + "when 'PLAYER' then 'PLAYER' when 'ZOMBIE' then 'ZOMBIE' "
        + "when 'ESQUELETO' then 'ESQUELETO' when 'LOBO' then 'LOBO' "
        + "when 'OVEJA' then 'OVEJA' when 'ALDEANO' then 'ALDEANO' "
        + "when 'CERDO' then 'CERDO' when 'ZOMBIEPEQUEÑO' then 'ZOMBIE_PEQUENO' "
        + "when 'ZOMBIEPEQUENO' then 'ZOMBIE_PEQUENO' "
        + "when 'ZOMBIE PEQUEÑO' then 'ZOMBIE_PEQUENO' "
        + "when 'ZOMBIE PEQUENO' then 'ZOMBIE_PEQUENO' "
        + "when 'ZOMBIE_PEQUEÑO' then 'ZOMBIE_PEQUENO' "
        + "when 'ZOMBIE_PEQUENO' then 'ZOMBIE_PEQUENO' else 'OTRA' end)")
public abstract class Entidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int vida;
    @Column(nullable = false)
    private String nombre;
    @ElementCollection
    @CollectionTable(name = "entidad_sonidos", joinColumns = @JoinColumn(name = "entidad_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "sonido", nullable = false)
    private List<String> sonidos = new ArrayList<>();
    @Embedded
    private Hitbox hitbox;
    @Column(nullable = false)
    private double velocidadMovimiento;
    @ElementCollection
    @CollectionTable(name = "entidad_drops", joinColumns = @JoinColumn(name = "entidad_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "objeto", nullable = false)
    private List<String> drop = new ArrayList<>();
    @Column(nullable = false)
    private int danoBase;
    @Enumerated(EnumType.STRING)
    private TipoEntidad tipo;
    @Embedded
    private Posicion posicion;

    /** Solo JPA puede hidratar una instancia sin pasar datos de negocio. */
    protected Entidad() {
    }

    /** Constructor con daño base igual a cero (mantiene compatibilidad con las subclases existentes). */
    protected Entidad(String nombre, int vida, Hitbox hitbox,
                       List<String> sonidos, List<String> drop,
                       double velocidadMovimiento) {
        this(nombre, vida, hitbox, sonidos, drop, velocidadMovimiento, 0);
    }

    /** Constructor completo que recibe también el daño base de la entidad. */
    protected Entidad(String nombre, int vida, Hitbox hitbox,
                       List<String> sonidos, List<String> drop,
                       double velocidadMovimiento, int danoBase) {
        this.nombre = nombre;
        this.vida = vida;
        this.hitbox = hitbox;
        this.sonidos = new ArrayList<>(sonidos);
        this.drop = new ArrayList<>(drop);
        this.velocidadMovimiento = velocidadMovimiento;
        this.danoBase = danoBase;
        this.tipo = TipoEntidad.desdeNombre(nombre);
    }

    /** Copia de estado; el service verifica datos y compatibilidad del subtipo antes de llamar. */
    public void actualizarDesde(Entidad datos) {
        nombre = datos.nombre;
        vida = datos.vida;
        danoBase = datos.danoBase;
        velocidadMovimiento = datos.velocidadMovimiento;
        hitbox = datos.hitbox;
        tipo = datos.tipo;
        posicion = datos.posicion;
        sonidos.clear();
        sonidos.addAll(datos.sonidos);
        drop.clear();
        drop.addAll(datos.drop);
        if (this instanceof Creeper creeper) creeper.actualizarCarga(datos.isCargado());
    }

    @Schema(description = "ID asignado por H2; puede ser null en demostraciones", accessMode = Schema.AccessMode.READ_ONLY)
    public Long getId() { return id; }
    public TipoEntidad getTipo() { return tipo != null ? tipo : TipoEntidad.desdeNombre(nombre); }
    public Posicion getPosicion() { return posicion; }
    public boolean isCargado() { return false; }

    /** Asignación sin reglas: solo la capa service debe construir estados de negocio. */
    public void asignarEstado(String nombre, int vida, int dano, double velocidad, Hitbox hitbox,
            List<String> sonidos, List<String> drop, TipoEntidad tipo, Posicion posicion) {
        this.nombre = nombre;
        this.vida = vida;
        this.danoBase = dano;
        this.velocidadMovimiento = velocidad;
        this.hitbox = hitbox;
        this.sonidos = new ArrayList<>(sonidos);
        this.drop = new ArrayList<>(drop);
        this.tipo = tipo;
        this.posicion = posicion;
    }

    public void setVida(int vida) { this.vida = vida; }
    protected void setTipo(TipoEntidad tipo) { this.tipo = tipo; }

    // ---- Getters (las listas son inmutables) ----

    public int getVida() {
        return vida;
    }

    public String getNombre() {
        return nombre;
    }

    public List<String> getSonidos() {
        return List.copyOf(sonidos);
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public double getVelocidadMovimiento() {
        return velocidadMovimiento;
    }

    public int getDanoBase() {
        return danoBase;
    }

    public List<String> getDrop() {
        return List.copyOf(drop);
    }

    public boolean isViva() {
        return vida > 0;
    }

    @Override
    public String toString() {
        return String.format("%s [vida=%d, danoBase=%d, velocidad=%.2f, hitbox=%s, drop=%s]",
                nombre, vida, danoBase, velocidadMovimiento, hitbox, drop);
    }
}
