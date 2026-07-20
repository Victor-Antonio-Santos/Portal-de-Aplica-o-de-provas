package br.com.empresa.provas.util;

public final class CPFUtil {

    private CPFUtil() {
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        String valor = somenteDigitos(cpf);
        if (valor.length() != 11 || valor.matches("(\\d)\\1{10}")) {
            return false;
        }

        return calcularDigito(valor, 9) == Character.getNumericValue(valor.charAt(9))
                && calcularDigito(valor, 10) == Character.getNumericValue(valor.charAt(10));
    }

    private static int calcularDigito(String cpf, int tamanho) {
        int soma = 0;
        int peso = tamanho + 1;
        for (int i = 0; i < tamanho; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (peso - i);
        }
        int resto = 11 - (soma % 11);
        return resto > 9 ? 0 : resto;
    }

    public static String formatar(String cpf) {
        String valor = somenteDigitos(cpf);
        if (valor.length() != 11) {
            return valor;
        }
        return valor.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}

