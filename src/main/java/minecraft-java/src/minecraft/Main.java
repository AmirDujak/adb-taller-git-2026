package minecraft;

import java.util.List;

/**
 * Clase de prueba que arma el "mundo" con entidades hostiles y no hostiles
 * y demuestra el uso de la jerarquía generada a partir del diagrama.
 */
public class Main {

    public static void main(String[] args) {

        List<Hostiles> hostiles = List.of(new Creeper(), new Zombie(), new Esqueleto());
        List<NoHostiles> pacificos = List.of(new Lobo(), new Oveja(), new Aldeano("Herrero"));

        System.out.println("=== Mobs hostiles ===");
        for (Hostiles h : hostiles) {
            System.out.println(h);
            h.avanzar();
            if (h.detecta(10.0)) {
                h.accionDeAtaque();
            }
            System.out.println();
        }

        System.out.println("=== Mobs no hostiles ===");
        for (NoHostiles nh : pacificos) {
            System.out.println(nh);
            nh.saltar();
            nh.interactuar();
            System.out.println();
        }

        System.out.println("=== Combate de ejemplo ===");
        Zombie zombie = (Zombie) hostiles.get(1);
        zombie.recibirDanio(25); // más que su vida -> muere y suelta el drop
    }
}
