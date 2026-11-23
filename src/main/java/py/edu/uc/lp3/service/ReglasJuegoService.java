package py.edu.uc.lp3.service;

import java.util.List;
import org.springframework.stereotype.Service;
import py.edu.uc.lp3.domain.*;
import py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException;
import py.edu.uc.lp3.exceptions.ExplosionNoPermitidaException;

/** Reglas locales compartidas por CRUD, explosiones y demos; las entidades solo guardan estado. */
@Service
public class ReglasJuegoService {
    public static final double RADIO_EXPLOSION = 3.0;
    private static final int DANO_MAXIMO_EXPLOSION = 20;

    public Entidad crear(String nombre, int vida, int dano, double velocidad, Hitbox hitbox,
            List<String> sonidos, List<String> drop, TipoEntidad tipo, boolean cargado, Posicion posicion) {
        if (tipo == null) throw new EstadoEntidadInvalidoException("El tipo es obligatorio");
        validarDatos(nombre, vida, dano, velocidad, hitbox, sonidos, drop, posicion);
        if (cargado && tipo != TipoEntidad.CREEPER)
            throw new EstadoEntidadInvalidoException("Solo un Creeper puede estar cargado");
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
        entidad.asignarEstado(nombre.strip(), vida, dano, velocidad, hitbox, sonidos, drop, tipo, posicion);
        if (entidad instanceof Creeper creeper) creeper.setCargado(cargado);
        validarInicial(entidad);
        return entidad;
    }

    public void validarInicial(Entidad entidad) {
        validarDatos(entidad.getNombre(), entidad.getVida(), entidad.getDanoBase(),
                entidad.getVelocidadMovimiento(), entidad.getHitbox(), entidad.getSonidos(),
                entidad.getDrop(), entidad.getPosicion());
        if (entidad instanceof Hostiles hostil
                && (!Double.isFinite(hostil.getRangoDeAggro()) || hostil.getRangoDeAggro() <= 0))
            throw new EstadoEntidadInvalidoException("El rango de aggro debe ser finito y positivo");
        if (entidad instanceof Aldeano aldeano && (aldeano.getProfesion() == null || aldeano.getProfesion().isBlank()))
            throw new EstadoEntidadInvalidoException("El aldeano necesita una profesión");
        if (entidad instanceof ZombiePequeño zombie && zombie.getVelocidadAumentada() < 0)
            throw new EstadoEntidadInvalidoException("La velocidad aumentada no puede ser negativa");
    }

    private void validarDatos(String nombre, int vida, int dano, double velocidad, Hitbox hitbox,
            List<String> sonidos, List<String> drop, Posicion posicion) {
        if (nombre == null || nombre.isBlank() || nombre.strip().length() > 255)
            throw new EstadoEntidadInvalidoException("El nombre es obligatorio y debe tener hasta 255 caracteres");
        if (vida <= 0) throw new EstadoEntidadInvalidoException("La vida inicial debe ser positiva");
        if (dano < 0) throw new EstadoEntidadInvalidoException("El daño base no puede ser negativo");
        if (!Double.isFinite(velocidad) || velocidad < 0)
            throw new EstadoEntidadInvalidoException("La velocidad debe ser finita y no negativa");
        if (hitbox == null) throw new EstadoEntidadInvalidoException("La hitbox es obligatoria");
        for (double dimension : new double[]{hitbox.getAncho(), hitbox.getAlto(), hitbox.getProfundidad()}) {
            if (!Double.isFinite(dimension) || dimension <= 0)
                throw new EstadoEntidadInvalidoException("Las dimensiones de la hitbox deben ser finitas y positivas");
        }
        for (List<String> lista : java.util.Arrays.asList(sonidos, drop)) {
            if (lista == null || lista.stream().anyMatch(s -> s == null || s.isBlank() || s.length() > 255))
                throw new EstadoEntidadInvalidoException("Sonidos y drops requieren listas de textos válidos");
        }
        if (posicion != null) validarPosicion(posicion);
    }

    public void validarPosicion(Posicion posicion) {
        if (posicion == null || !Double.isFinite(posicion.getX()) || !Double.isFinite(posicion.getY())
                || !Double.isFinite(posicion.getZ()))
            throw new EstadoEntidadInvalidoException("Las coordenadas deben ser finitas");
    }

    public double distancia(Posicion origen, Posicion destino) {
        validarPosicion(origen);
        validarPosicion(destino);
        return Math.hypot(Math.hypot(origen.getX() - destino.getX(), origen.getY() - destino.getY()),
                origen.getZ() - destino.getZ());
    }

    public Creeper prepararExplosion(Entidad entidad) {
        if (!(entidad instanceof Creeper creeper))
            throw new ExplosionNoPermitidaException("La entidad indicada no es un Creeper");
        if (!creeper.isViva() || creeper.getPosicion() == null)
            throw new ExplosionNoPermitidaException("El Creeper necesita estar vivo y tener una posición guardada para explotar");
        validarPosicion(creeper.getPosicion());
        return creeper;
    }

    public int calcularDanioExplosion(Creeper creeper, double distancia) {
        validarDistancia(distancia);
        if (!creeper.isViva() || distancia >= RADIO_EXPLOSION) return 0;
        int maximo = DANO_MAXIMO_EXPLOSION * (creeper.isCargado() ? 2 : 1);
        return (int) Math.round(maximo * (1 - distancia / RADIO_EXPLOSION));
    }

    public boolean detecta(Hostiles entidad, double distancia) {
        validarDistancia(distancia);
        return distancia <= entidad.getRangoDeAggro();
    }

    private void validarDistancia(double distancia) {
        if (!Double.isFinite(distancia) || distancia < 0)
            throw new EstadoEntidadInvalidoException("La distancia debe ser finita y no negativa");
    }

    public String recibirDanio(Entidad entidad, int cantidad) {
        if (cantidad < 0) throw new EstadoEntidadInvalidoException("El daño no puede ser negativo");
        if (!entidad.isViva()) return entidad.getNombre() + " ya desapareció, no puede recibir daño.";
        entidad.setVida(Math.max(0, entidad.getVida() - cantidad));
        String resultado = entidad.getNombre() + " recibe " + cantidad + " de daño"
                + (entidad.getSonidos().isEmpty() ? "" : " y hace el sonido: " + entidad.getSonidos().getFirst());
        if (!entidad.isViva()) resultado += ". " + retirarDelMundo(entidad);
        return resultado;
    }

    public String retirarDelMundo(Entidad entidad) {
        entidad.setVida(0);
        return entidad.getNombre() + " ha desaparecido y suelta: " + entidad.getDrop();
    }

    /** El adaptador de servicio implementa Movimientos, no la Entity de persistencia. */
    public Movimientos movimientos(Entidad entidad) {
        return new Movimientos() {
            private String noPuedeMoverse() { return entidad.getNombre() + " ya desapareció, no puede moverse."; }
            public String avanzar() {
                if (!entidad.isViva()) return noPuedeMoverse();
                return entidad instanceof Creeper ? entidad.getNombre() + " se acerca sigilosamente, sin hacer ruido"
                        : entidad.getNombre() + " avanza a velocidad " + entidad.getVelocidadMovimiento();
            }
            public String saltar() { return entidad.isViva() ? entidad.getNombre() + " salta" : noPuedeMoverse(); }
            public String rotar(double angulo) {
                return entidad.isViva() ? entidad.getNombre() + " rota " + angulo + " grados" : noPuedeMoverse();
            }
        };
    }

    public String comportamiento(Entidad entidad) {
        String nombre = entidad.getNombre();
        if (!entidad.isViva()) return nombre + (entidad instanceof Player ? " ya fue derrotado." : " ya desapareció.");
        if (entidad instanceof Creeper creeper) {
            return nombre + " explota" + (creeper.isCargado() ? " ¡con carga extra!" : "") + ". " + retirarDelMundo(creeper);
        }
        if (entidad instanceof Player) return nombre + " explora el mundo y combate a las entidades.";
        if (entidad instanceof Zombie zombie)
            return nombre + (zombie.isHostilidad() ? " golpea cuerpo a cuerpo al jugador." : " deambula sin atacar.");
        if (entidad instanceof Esqueleto) return nombre + " dispara una flecha a distancia.";
        if (entidad instanceof Aldeano aldeano) return nombre + " (" + aldeano.getProfesion() + ") abre su menú de comercio.";
        if (entidad instanceof Lobo lobo) {
            if (lobo.isDomesticado()) return nombre + " menea la cola, ya es tu mascota.";
            lobo.setDomesticado(true);
            return nombre + " fue domesticado con huesos.";
        }
        if (entidad instanceof Oveja oveja) {
            if (oveja.isEsquilada()) return nombre + " ya no tiene lana para esquilar.";
            oveja.setEsquilada(true);
            return nombre + " fue esquilada, se obtiene lana.";
        }
        if (entidad instanceof Cerdo cerdo)
            return nombre + (cerdo.isMontable() ? " puede ser montado por el jugador." : " no se puede montar.");
        return nombre + " recorre el mundo.";
    }

    public String disparar(Esqueleto entidad) { return disparar(entidad, null); }

    public String disparar(Esqueleto entidad, Integer distancia) {
        if (distancia != null && distancia < 0)
            throw new EstadoEntidadInvalidoException("La distancia no puede ser negativa");
        if (!entidad.isViva()) return entidad.getNombre() + " ya desapareció.";
        return entidad.getNombre() + " dispara una flecha" + (distancia == null ? "" : " a " + distancia + " bloques")
                + (entidad.isUsaArco() ? " con arco" : "") + ".";
    }
}
