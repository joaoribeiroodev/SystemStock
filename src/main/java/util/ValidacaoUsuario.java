package util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class ValidacaoUsuario {

    private static final LocalDate DATA_NASCIMENTO_MIN = LocalDate.of(1900, 1, 1);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private ValidacaoUsuario() {
    }

    /** @return null se válida, ou mensagem de erro */
    public static String validarDataNascimento(String data) {
        if (data == null || data.isBlank()) {
            return "Data de nascimento é obrigatória.";
        }

        try {
            LocalDate nascimento = LocalDate.parse(data.trim());
            LocalDate hoje = LocalDate.now(FUSO);

            if (nascimento.isBefore(DATA_NASCIMENTO_MIN)) {
                return "Data de nascimento deve ser a partir de 01/01/1900.";
            }
            if (nascimento.isAfter(hoje)) {
                return "Data de nascimento não pode ser no futuro.";
            }
            return null;
        } catch (DateTimeParseException e) {
            return "Data de nascimento inválida.";
        }
    }
}
