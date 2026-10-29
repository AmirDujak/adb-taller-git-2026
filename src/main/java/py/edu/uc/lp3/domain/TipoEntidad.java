package py.edu.uc.lp3.domain;

import java.util.Locale;

public enum TipoEntidad {
    CREEPER, PLAYER, ZOMBIE, ESQUELETO, LOBO, OVEJA, ALDEANO, CERDO, ZOMBIE_PEQUENO, OTRA;

    /** Compatibilidad con los registros antiguos que solo guardaban el nombre. */
    public static TipoEntidad desdeNombre(String nombre) {
        String normalizado = nombre.strip().toUpperCase(Locale.ROOT)
                .replace('Ñ', 'N').replace(" ", "_");
        if (normalizado.equals("ZOMBIEPEQUENO")) {
            return ZOMBIE_PEQUENO;
        }
        try {
            return valueOf(normalizado);
        } catch (IllegalArgumentException e) {
            return OTRA;
        }
    }
}
