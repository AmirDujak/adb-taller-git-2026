package py.edu.uc.lp3.demo;

import java.util.List;
import py.edu.uc.lp3.domain.*;
import py.edu.uc.lp3.service.ReglasJuegoService;

/**
 * Clase de prueba que arma el "mundo" con entidades hostiles y no hostiles
 * y las trata a todas de forma uniforme a través del tipo padre Entidad.
 */
public class Main {

    public static void main(String[] args) {

        List<Entidad> mundo = List.of(new Creeper(), new Zombie(), new Esqueleto(),
                new Lobo(), new Oveja(), new Aldeano("Herrero"));
        var reglas = new ReglasJuegoService();

        for (Entidad e : mundo) {
            System.out.println(e);
            System.out.println(reglas.movimientos(e).avanzar());
            System.out.println(reglas.movimientos(e).saltar());
            System.out.println(reglas.comportamiento(e));
            System.out.println(reglas.recibirDanio(e, 25));
            System.out.println();
        }
    }
}
