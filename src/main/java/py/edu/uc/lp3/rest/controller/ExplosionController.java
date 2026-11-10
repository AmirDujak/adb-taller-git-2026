package py.edu.uc.lp3.rest.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.rest.dto.ExplosionDatos;
import py.edu.uc.lp3.rest.dto.ExplosionResultado;
import py.edu.uc.lp3.service.ExplosionService;

@RestController
@RequestMapping(ApiPaths.EXPLOSIONES)
public class ExplosionController {

    private final ExplosionService service;

    public ExplosionController(ExplosionService service) {
        this.service = service;
    }

    @PostMapping("/simular")
    public ExplosionResultado simular(@RequestBody ExplosionDatos datos) {
        return service.simular(datos);
    }

}
