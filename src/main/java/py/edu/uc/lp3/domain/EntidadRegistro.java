package py.edu.uc.lp3.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** Representación persistente de los atributos comunes de {@link Entidad}.
 * Incluye tipo y posición para aplicar las explosiones sobre el estado guardado.
 */
@Entity
@Table(name = "entidades")
public class EntidadRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private int vida;
    @Column(nullable = false)
    private int danoBase;
    @Column(nullable = false)
    private double velocidadMovimiento;

    // Columnas nuevas y anulables para conservar las filas de la base existente.
    @Enumerated(EnumType.STRING)
    private TipoEntidad tipo;
    private Boolean cargado;

    @Embedded
    private Posicion posicion;

    @Embedded
    private HitboxRegistro hitbox;

    @ElementCollection
    @CollectionTable(name = "entidad_sonidos", joinColumns = @JoinColumn(name = "entidad_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "sonido", nullable = false)
    private List<String> sonidos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "entidad_drops", joinColumns = @JoinColumn(name = "entidad_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "objeto", nullable = false)
    private List<String> drop = new ArrayList<>();

    protected EntidadRegistro() {
    }

    public EntidadRegistro(String nombre, int vida, int danoBase, double velocidadMovimiento,
                           HitboxRegistro hitbox, List<String> sonidos, List<String> drop) {
        this(nombre, vida, danoBase, velocidadMovimiento, hitbox, sonidos, drop,
                TipoEntidad.desdeNombre(nombre), false, null);
    }

    public EntidadRegistro(String nombre, int vida, int danoBase, double velocidadMovimiento,
                           HitboxRegistro hitbox, List<String> sonidos, List<String> drop,
                           TipoEntidad tipo, boolean cargado, Posicion posicion) {
        this.nombre = nombre;
        this.vida = vida;
        this.danoBase = danoBase;
        this.velocidadMovimiento = velocidadMovimiento;
        this.hitbox = hitbox;
        this.sonidos = new ArrayList<>(sonidos);
        this.drop = new ArrayList<>(drop);
        this.tipo = tipo;
        this.cargado = cargado;
        this.posicion = posicion;
    }

    public void actualizarDesde(EntidadRegistro datos) {
        nombre = datos.nombre;
        vida = datos.vida;
        danoBase = datos.danoBase;
        velocidadMovimiento = datos.velocidadMovimiento;
        hitbox = datos.hitbox;
        tipo = datos.tipo;
        cargado = datos.cargado;
        posicion = datos.posicion;
        sonidos.clear();
        sonidos.addAll(datos.sonidos);
        drop.clear();
        drop.addAll(datos.drop);
    }

    public Long getId() { return id; }
    public TipoEntidad getTipo() { return tipo != null ? tipo : TipoEntidad.desdeNombre(nombre); }
    public boolean isCargado() { return Boolean.TRUE.equals(cargado); }
    public Posicion getPosicion() { return posicion; }

    /** Mantiene la regla de vida no negativa también sobre los registros persistentes. */
    public int recibirDanio(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo");
        }
        int perdida = Math.min(vida, cantidad);
        vida -= perdida;
        return perdida;
    }

    public String getNombre() { return nombre; }
    public int getVida() { return vida; }
    public int getDanoBase() { return danoBase; }
    public double getVelocidadMovimiento() { return velocidadMovimiento; }
    public HitboxRegistro getHitbox() { return hitbox; }
    public List<String> getSonidos() { return List.copyOf(sonidos); }
    public List<String> getDrop() { return List.copyOf(drop); }
}
