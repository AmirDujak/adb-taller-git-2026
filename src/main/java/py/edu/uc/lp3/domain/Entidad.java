package py.edu.uc.lp3.domain;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import jakarta.persistence.*;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.Check;
import py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException;
import py.edu.uc.lp3.exceptions.ExplosionNoPermitidaException;

/**
 * Clase base "Entidades" del diagrama.
 * Toda entidad del juego (hostil o no hostil) hereda de aquí.
 *
 * Concentra las reglas comunes de vida, movimiento y muerte. Ningún campo
 * es accesible desde afuera: la vida solo cambia a través de
 * {@link #recibirDanio(int)} o {@link #desaparecer()}, así que no es
 * posible dejar una entidad en un estado imposible (vida negativa,
 * una entidad muerta que sigue moviéndose, etc.).
 */
@Entity
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
public abstract class Entidad implements Movimientos {

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
        validar(nombre, vida, danoBase, velocidadMovimiento, hitbox, sonidos, drop);
        this.nombre = nombre;
        this.vida = vida;
        this.hitbox = Objects.requireNonNull(hitbox, "hitbox");
        this.sonidos = new ArrayList<>(sonidos);
        this.drop = new ArrayList<>(drop);
        this.velocidadMovimiento = velocidadMovimiento;
        this.danoBase = danoBase;
        this.tipo = TipoEntidad.desdeNombre(nombre);
    }

    private static void validar(String nombre, int vida, int dano, double velocidad,
                                Hitbox hitbox, List<String> sonidos, List<String> drop) {
        if (nombre == null || nombre.isBlank() || nombre.strip().length() > 255)
            throw new EstadoEntidadInvalidoException("El nombre es obligatorio y debe tener hasta 255 caracteres");
        if (vida <= 0) throw new EstadoEntidadInvalidoException("La vida inicial debe ser positiva");
        if (dano < 0) throw new EstadoEntidadInvalidoException("El daño base no puede ser negativo");
        if (!Double.isFinite(velocidad) || velocidad < 0)
            throw new EstadoEntidadInvalidoException("La velocidad debe ser finita y no negativa");
        if (hitbox == null) throw new EstadoEntidadInvalidoException("La hitbox es obligatoria");
        for (List<String> lista : java.util.Arrays.asList(sonidos, drop)) {
            if (lista == null || lista.stream().anyMatch(s -> s == null || s.isBlank() || s.length() > 255))
                throw new EstadoEntidadInvalidoException("Sonidos y drops requieren listas de textos válidos");
        }
    }

    /** Fábrica de subtipos reales: el JSON no puede construir un tipo inconsistente. */
    public static Entidad crear(String nombre, int vida, int dano, double velocidad,
                                Hitbox hitbox, List<String> sonidos, List<String> drop,
                                TipoEntidad tipo, boolean cargado, Posicion posicion) {
        if (tipo == null) throw new EstadoEntidadInvalidoException("El tipo es obligatorio");
        Entidad entidad = switch (tipo) {
            case CREEPER -> new Creeper();
            case PLAYER -> new Player();
            case ZOMBIE -> new Zombie();
            case ESQUELETO -> new Esqueleto();
            case LOBO -> new Lobo();
            case OVEJA -> new Oveja();
            case ALDEANO -> new Aldeano("Granjero");
            case CERDO -> new Cerdo();
            case ZOMBIE_PEQUENO -> new ZombiePequeño();
            case OTRA -> new OtraEntidad();
        };
        validar(nombre, vida, dano, velocidad, hitbox, sonidos, drop);
        if (cargado && !(entidad instanceof Creeper))
            throw new EstadoEntidadInvalidoException("Solo un Creeper puede estar cargado");
        entidad.nombre = nombre.strip();
        entidad.vida = vida;
        entidad.danoBase = dano;
        entidad.velocidadMovimiento = velocidad;
        entidad.hitbox = hitbox;
        entidad.sonidos = new ArrayList<>(sonidos);
        entidad.drop = new ArrayList<>(drop);
        entidad.tipo = tipo;
        entidad.posicion = posicion;
        if (cargado) ((Creeper) entidad).cargarConRayo();
        return entidad;
    }

    public void actualizarDesde(Entidad datos) {
        if (getTipo() != datos.getTipo())
            throw new EstadoEntidadInvalidoException("No se puede cambiar el subtipo de una entidad existente");
        validar(datos.nombre, datos.vida, datos.danoBase, datos.velocidadMovimiento,
                datos.hitbox, datos.sonidos, datos.drop);
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

    public Long getId() { return id; }
    public TipoEntidad getTipo() { return tipo != null ? tipo : TipoEntidad.desdeNombre(nombre); }
    public Posicion getPosicion() { return posicion; }
    public boolean isCargado() { return false; }

    public Creeper prepararExplosion() {
        throw new ExplosionNoPermitidaException("La entidad indicada no es un Creeper");
    }

    /** Borrar significa retirar definitivamente la entidad del mundo. */
    public void retirarDelMundo() { desaparecer(); }

    @PrePersist
    @PreUpdate
    private void validarPersistencia() {
        tipo = getTipo();
        validar(nombre, vida, danoBase, velocidadMovimiento, hitbox, sonidos, drop);
        if (isCargado() && !(this instanceof Creeper))
            throw new EstadoEntidadInvalidoException("Solo un Creeper puede estar cargado");
    }

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

    // ---- Comportamiento propio de cada entidad ----

    /**
     * Lo que hace la entidad cuando el jugador está cerca: cada rama de la
     * jerarquía lo define (un Creeper explota, un Aldeano comercia...).
     */
    public abstract String comportamiento();

    // ---- Reglas comunes de vida y muerte ----

    public final String recibirDanio(int cantidad) {
        if (cantidad < 0) {
            throw new EstadoEntidadInvalidoException("El daño no puede ser negativo");
        }
        if (!isViva()) {
            return nombre + " ya desapareció, no puede recibir daño.";
        }
        vida = Math.max(0, vida - cantidad);
        String resultado = nombre + " recibe " + cantidad + " de daño"
                + (sonidos.isEmpty() ? "" : " y hace el sonido: " + sonidos.get(0));
        if (!isViva()) {
            resultado += ". " + desaparecer();
        }
        return resultado;
    }

    /**
     * Mata a la entidad y suelta su drop. Es protected porque solo la
     * jerarquía decide cuándo una entidad desaparece sin recibir daño
     * (por ejemplo, un Creeper al explotar).
     */
    protected final String desaparecer() {
        vida = 0;
        return nombre + " ha desaparecido y suelta: " + drop;
    }

    // ---- Implementación por defecto de Movimientos ----
    // Una entidad muerta no puede moverse. Las subclases solo redefinen
    // la parte que realmente cambia a través de los métodos "describir...".

    @Override
    public final String avanzar() {
        return isViva() ? describirAvance() : noPuedeMoverse();
    }

    @Override
    public final String rotar(double angulo) {
        return isViva() ? nombre + " rota " + angulo + " grados" : noPuedeMoverse();
    }

    @Override
    public final String saltar() {
        return isViva() ? nombre + " salta" : noPuedeMoverse();
    }

    /** Punto de extensión: cómo avanza esta entidad en particular. */
    protected String describirAvance() {
        return nombre + " avanza a velocidad " + velocidadMovimiento;
    }

    private String noPuedeMoverse() {
        return nombre + " ya desapareció, no puede moverse.";
    }

    @Override
    public String toString() {
        return String.format("%s [vida=%d, danoBase=%d, velocidad=%.2f, hitbox=%s, drop=%s]",
                nombre, vida, danoBase, velocidadMovimiento, hitbox, drop);
    }
}
