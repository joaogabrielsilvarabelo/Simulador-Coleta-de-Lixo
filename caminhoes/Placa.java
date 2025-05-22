package caminhoes;

import java.util.Random;

class Placa {
    private static final String PLACA_REGEX = "^[A-Z]{3}[0-9][A-Z][0-9]{2}$";

    static String processarPlaca(String placaOpcional) {
        if (placaOpcional != null && !placaOpcional.isBlank()) {
            if (!validarPlaca(placaOpcional)) {
                throw new IllegalArgumentException("Placa não segue normas do Mercosul");
            }
            return placaOpcional.toUpperCase();
        }
        return gerarPlaca();
    }

    static String gerarPlaca() {
        Random random = new Random();
        StringBuilder placa = new StringBuilder();
        // Mercosul: LLLNLNN
        for (int i = 0; i < 3; i++) {
            placa.append((char) (random.nextInt(26) + 'A'));
        }
        placa.append(random.nextInt(10));
        placa.append((char) (random.nextInt(26) + 'A'));
        placa.append(random.nextInt(10));
        placa.append(random.nextInt(10));
        return placa.toString();
    }

    static boolean validarPlaca(String placa) {
        return placa != null && placa.matches(PLACA_REGEX);
    }
}


