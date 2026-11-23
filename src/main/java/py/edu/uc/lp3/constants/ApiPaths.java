package py.edu.uc.lp3.constants;

/** Rutas centralizadas de la API REST, siguiendo el template de LP3. */
public final class ApiPaths {

    private static final String BASE_API = "/api/lp3";
    public static final String ENTIDADES = BASE_API + "/entidades";
    public static final String ENTIDADES_COMPAT = "/api/entidades";
    public static final String EXPLOSIONES = BASE_API + "/explosiones";

    public static final String ENTIDAD_POR_TIPO = "/entidad/{tipo}";
    public static final String DEMO_ENTIDADES = "/entidades";
    public static final String DEMO_ZOMBIE = DEMO_ENTIDADES + "/zombie";
    public static final String DEMO_DISPARAR = DEMO_ENTIDADES + "/esqueleto/disparar";
    public static final String DEMO_COMPORTAMIENTOS = DEMO_ENTIDADES + "/comportamientos";

    private ApiPaths() {
    }
}
