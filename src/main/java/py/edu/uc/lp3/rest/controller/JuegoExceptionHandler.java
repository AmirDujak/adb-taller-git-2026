package py.edu.uc.lp3.rest.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import py.edu.uc.lp3.exceptions.*;

@RestControllerAdvice
public class JuegoExceptionHandler {
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String codigo, Exception e) {
        return ResponseEntity.status(status).body(Map.of("codigo", codigo, "error", e.getMessage()));
    }

    @ExceptionHandler(EstadoEntidadInvalidoException.class)
    public ResponseEntity<Map<String, String>> estado(EstadoEntidadInvalidoException e) {
        return error(HttpStatus.BAD_REQUEST, "ESTADO_ENTIDAD_INVALIDO", e);
    }

    @ExceptionHandler(ExplosionNoPermitidaException.class)
    public ResponseEntity<Map<String, String>> explosion(ExplosionNoPermitidaException e) {
        return error(HttpStatus.CONFLICT, "EXPLOSION_NO_PERMITIDA", e);
    }

    @ExceptionHandler(JuegoException.class)
    public ResponseEntity<Map<String, String>> juego(JuegoException e) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, "REGLA_JUEGO", e);
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> inexistente(EntidadNoEncontradaException e) {
        return error(HttpStatus.NOT_FOUND, "ENTIDAD_NO_ENCONTRADA", e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> json(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("codigo", "JSON_INVALIDO", "error",
                "JSON inválido, tipo desconocido o cuerpo ausente"));
    }
}
