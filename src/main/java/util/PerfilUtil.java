package util;

public final class PerfilUtil {

    public static final String ADMIN  = "ADMIN";
    public static final String TESTER = "TESTER";

    private PerfilUtil() {
    }

    public static boolean isAdmin(String perfil) {
        return ADMIN.equalsIgnoreCase(normalizar(perfil));
    }

    public static boolean isTester(String perfil) {
        return TESTER.equalsIgnoreCase(normalizar(perfil));
    }

    public static boolean podeAcessarSistema(String perfil) {
        return isAdmin(perfil) || isTester(perfil);
    }

    public static boolean podeCadastrarUsuario(String perfil) {
        return isAdmin(perfil);
    }

    private static String normalizar(String perfil) {
        return perfil == null ? "" : perfil.trim();
    }
}
