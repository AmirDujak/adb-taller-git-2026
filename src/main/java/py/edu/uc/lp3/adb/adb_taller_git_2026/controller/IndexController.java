package py.edu.uc.lp3.adb.adb_taller_git_2026.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "Bienvenido al taller de Git 2026 - LP3. "
                + "Probá GET /entidad/{tipo} (ej: /entidad/zombie o /entidad/aldeano?profesion=Herrero) o GET /entidades";
    }
}
