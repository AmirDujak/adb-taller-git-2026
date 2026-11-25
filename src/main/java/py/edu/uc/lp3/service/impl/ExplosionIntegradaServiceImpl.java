package py.edu.uc.lp3.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;
import py.edu.uc.lp3.domain.Creeper;
import py.edu.uc.lp3.domain.Posicion;
import py.edu.uc.lp3.exceptions.ConsultaMinecraftException;
import py.edu.uc.lp3.exceptions.ConsultaMinecraftException.Motivo;
import py.edu.uc.lp3.exceptions.EstadoEntidadInvalidoException;
import py.edu.uc.lp3.rest.dto.EntidadRemotaDatos;
import py.edu.uc.lp3.rest.dto.ActualizarSaludRemotaDatos;
import py.edu.uc.lp3.rest.dto.ExplosionAplicadaResultado;
import py.edu.uc.lp3.rest.dto.ExplosionAplicadaResultado.AplicacionObjetivo;
import py.edu.uc.lp3.rest.dto.ExplosionAplicadaResultado.Estado;
import py.edu.uc.lp3.rest.dto.ExplosionIntegradaDatos;
import py.edu.uc.lp3.rest.dto.ExplosionIntegradaResultado;
import py.edu.uc.lp3.rest.dto.ExplosionIntegradaResultado.ObjetivoIntegrado;
import py.edu.uc.lp3.service.EntidadRemotaService;
import py.edu.uc.lp3.service.EntidadService;
import py.edu.uc.lp3.service.ExplosionIntegradaService;
import py.edu.uc.lp3.service.ReglasJuegoService;

@Service
public class ExplosionIntegradaServiceImpl implements ExplosionIntegradaService {

    private final EntidadService entidades;
    private final EntidadRemotaService remotas;
    private final ReglasJuegoService reglas;

    public ExplosionIntegradaServiceImpl(EntidadService entidades, EntidadRemotaService remotas, ReglasJuegoService reglas) {
        this.entidades = entidades;
        this.remotas = remotas;
        this.reglas = reglas;
    }

    @Override
    public ExplosionIntegradaResultado previsualizar(ExplosionIntegradaDatos datos) {
        Evaluacion evaluacion = evaluar(datos);
        Creeper creeper = evaluacion.creeper();
        return new ExplosionIntegradaResultado("LOCALES", ReglasJuegoService.RADIO_EXPLOSION,
                creeper.getId(), creeper.isCargado(), creeper.getVida(),
                evaluacion.objetivos().stream().map(ObjetivoEvaluado::estimacion).toList());
    }

    @Override
    public ExplosionAplicadaResultado aplicar(ExplosionIntegradaDatos datos) {
        // Se validan ambos participantes antes del primer PUT. No se abre una transacción local.
        Evaluacion evaluacion = evaluar(datos);
        List<AplicacionObjetivo> resultados = new ArrayList<>();
        boolean completa = true;
        for (ObjetivoEvaluado objetivo : evaluacion.objetivos()) {
            var estimacion = objetivo.estimacion();
            if (estimacion.danioAplicado() == 0) {
                resultados.add(new AplicacionObjetivo(estimacion, Estado.SIN_CAMBIOS,
                        estimacion.saludAnterior(), null, "No requiere actualización remota"));
            } else if (!completa) {
                resultados.add(new AplicacionObjetivo(estimacion, Estado.NO_INTENTADO, null,
                        "ACTUALIZACION_NO_INTENTADA", "No se intentó tras un fallo previo"));
            } else {
                try {
                    var original = objetivo.entidad();
                    var actualizada = remotas.actualizarSalud(original.id(), new ActualizarSaludRemotaDatos(
                            original.nombre(), original.tipo(), estimacion.saludEstimada(), original.ubicacion()));
                    validarRespuesta(actualizada, original.id());
                    if (!original.tipo().equals(actualizada.tipo())
                            || Double.compare(actualizada.salud(), estimacion.saludEstimada()) != 0) {
                        throw respuestaInvalida();
                    }
                    resultados.add(new AplicacionObjetivo(estimacion, Estado.CONFIRMADO,
                            actualizada.salud(), null, "Salud actualizada y confirmada por el servidor remoto"));
                } catch (ConsultaMinecraftException e) {
                    completa = false;
                    String codigo = switch (e.getMotivo()) {
                        case NO_ENCONTRADA -> "ENTIDAD_REMOTA_NO_ENCONTRADA";
                        case NO_DISPONIBLE -> "API_MINECRAFT_NO_DISPONIBLE";
                        case TIEMPO_AGOTADO -> "API_MINECRAFT_TIMEOUT";
                    };
                    resultados.add(new AplicacionObjetivo(estimacion, Estado.NO_CONFIRMADO, null, codigo,
                            e.getMessage() + ". El PUT pudo haberse aplicado; consulte el estado remoto antes de repetir."));
                }
            }
        }
        return new ExplosionAplicadaResultado("LOCALES", ReglasJuegoService.RADIO_EXPLOSION,
                evaluacion.creeper().getId(), completa, List.copyOf(resultados));
    }

    private record ObjetivoEvaluado(EntidadRemotaDatos entidad, ObjetivoIntegrado estimacion) { }
    private record Evaluacion(Creeper creeper, List<ObjetivoEvaluado> objetivos) { }

    private Evaluacion evaluar(ExplosionIntegradaDatos datos) {
        if (datos == null || datos.creeperId() == null || datos.creeperId() <= 0
                || datos.jugadorExternoId() == null || datos.cerdoExternoId() == null) {
            throw new EstadoEntidadInvalidoException("Se requieren creeperId positivo y los UUID del jugador y cerdo externos");
        }
        if (datos.jugadorExternoId().equals(datos.cerdoExternoId())) {
            throw new EstadoEntidadInvalidoException("El jugador y el cerdo externos deben tener UUID distintos");
        }
        // La búsqueda local termina su transacción antes de consultar la red.
        Creeper creeper = reglas.prepararExplosion(entidades.buscar(datos.creeperId()));
        var jugador = consultarObjetivo(creeper, datos.jugadorExternoId(), "JUGADOR");
        var cerdo = consultarObjetivo(creeper, datos.cerdoExternoId(), "CERDO");
        return new Evaluacion(creeper, List.of(jugador, cerdo));
    }

    private ObjetivoEvaluado consultarObjetivo(Creeper creeper, UUID id, String tipoEsperado) {
        EntidadRemotaDatos entidad = remotas.buscar(id);
        validarRespuesta(entidad, id);
        if (!tipoEsperado.equals(entidad.tipo())) {
            throw new EstadoEntidadInvalidoException("La entidad remota " + id + " debe ser de tipo " + tipoEsperado);
        }
        var u = entidad.ubicacion();
        double distancia = reglas.distancia(creeper.getPosicion(), new Posicion(u.x(), u.y(), u.z()));
        if (!Double.isFinite(distancia)) {
            throw respuestaInvalida();
        }
        int calculado = reglas.calcularDanioExplosion(creeper, distancia);
        double aplicado = Math.min(entidad.salud(), calculado);
        double estimada = Math.max(0, entidad.salud() - aplicado);
        return new ObjetivoEvaluado(entidad, new ObjetivoIntegrado(entidad.id(), entidad.nombre(), entidad.tipo(), distancia,
                calculado, aplicado, entidad.salud(), estimada, estimada > 0));
    }

    private void validarRespuesta(EntidadRemotaDatos entidad, UUID id) {
        if (entidad == null || !id.equals(entidad.id()) || entidad.nombre() == null || entidad.nombre().isBlank()
                || entidad.tipo() == null || !finito(entidad.salud()) || entidad.salud() < 0
                || entidad.ubicacion() == null || !finito(entidad.ubicacion().x())
                || !finito(entidad.ubicacion().y()) || !finito(entidad.ubicacion().z())) {
            throw respuestaInvalida();
        }
    }

    private boolean finito(Double valor) {
        return valor != null && Double.isFinite(valor);
    }

    private ConsultaMinecraftException respuestaInvalida() {
        return new ConsultaMinecraftException(Motivo.NO_DISPONIBLE,
                "La API Minecraft remota devolvió datos incompletos o inválidos", null);
    }
}
