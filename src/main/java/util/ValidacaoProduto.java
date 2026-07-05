package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class ValidacaoProduto {

    private static final ZoneId FUSO_SISTEMA = ZoneId.of("America/Sao_Paulo");

    private ValidacaoProduto() {}

    public static LocalDate hoje() {
        return LocalDate.now(FUSO_SISTEMA);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static Long parseLongPositivo(String value) {
        if (isBlank(value)) return null;
        try {
            long n = Long.parseLong(value.trim());
            return n > 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal parseValorPositivo(String value) {
        if (isBlank(value)) return null;
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            return bd.compareTo(BigDecimal.ZERO) > 0 ? bd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String validarDatas(String dataFabricacao, String dataVencimento) {
        LocalDate hoje = hoje();
        LocalDate fab;
        LocalDate ven;

        try {
            fab = LocalDate.parse(dataFabricacao);
            ven = LocalDate.parse(dataVencimento);
        } catch (DateTimeParseException e) {
            return "Datas inválidas.";
        }

        if (fab.isAfter(hoje)) {
            return "Data de fabricação não pode ser posterior à data atual.";
        }
        if (!ven.isAfter(hoje)) {
            return "Data de vencimento deve ser posterior à data atual.";
        }
        if (ven.isBefore(fab)) {
            return "Data de vencimento não pode ser anterior à data de fabricação.";
        }

        return null;
    }
}
