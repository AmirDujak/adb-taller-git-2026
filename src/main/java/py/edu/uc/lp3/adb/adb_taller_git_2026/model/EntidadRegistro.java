package py.edu.uc.lp3.adb.adb_taller_git_2026.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** Representación persistente de los atributos comunes de minecraft.Entidad.
 * No reemplaza la jerarquía del juego ni ejecuta sus comportamientos.
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
        this.nombre = nombre;
        this.vida = vida;
        this.danoBase = danoBase;
        this.velocidadMovimiento = velocidadMovimiento;
        this.hitbox = hitbox;
        this.sonidos = new ArrayList<>(sonidos);
        this.drop = new ArrayList<>(drop);
    }

    public void actualizarDesde(EntidadRegistro datos) {
        nombre = datos.nombre;
        vida = datos.vida;
        danoBase = datos.danoBase;
        velocidadMovimiento = datos.velocidadMovimiento;
        hitbox = datos.hitbox;
        sonidos.clear();
        sonidos.addAll(datos.sonidos);
        drop.clear();
        drop.addAll(datos.drop);
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getVida() { return vida; }
    public int getDanoBase() { return danoBase; }
    public double getVelocidadMovimiento() { return velocidadMovimiento; }
    public HitboxRegistro getHitbox() { return hitbox; }
    public List<String> getSonidos() { return List.copyOf(sonidos); }
    public List<String> getDrop() { return List.copyOf(drop); }
}
