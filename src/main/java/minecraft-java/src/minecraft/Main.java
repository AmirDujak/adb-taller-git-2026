package minecraft;

import java.util.List;

/**
 * Clase de prueba que arma el "mundo" con entidades hostiles y no hostiles
 * y las trata a todas de forma uniforme a través del tipo padre Entidad.
 */
public class Main {

    public static void main(String[] args) {

        List<Entidad> mundo = List.of(new Creeper(), new Zombie(), new Esqueleto(),
                new Lobo(), new Oveja(), new Aldeano("Herrero"));

        for (Entidad e : mundo) {
            System.out.println(e);
            System.out.println(e.avanzar());
            System.out.println(e.saltar());
            System.out.println(e.comportamiento());
            System.out.println(e.recibirDanio(25));
            System.out.println();
        }
    }
}
