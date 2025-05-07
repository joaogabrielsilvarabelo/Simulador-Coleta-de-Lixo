package caminhoes;

import java.util.Random;

public class Placa {
    protected static String gerarPlaca(){
        Random random = new Random();
        StringBuilder placa = new StringBuilder();
        //Placa Mercosul = LLLNLNN
        //LLL
        for (int i = 0; i < 3; i++) {
            placa.append((char) (random.nextInt(26) + 'A'));
        }
        //N
        placa.append(random.nextInt(10));
        //L
        placa.append((char) (random.nextInt(26) + 'A'));
        //NN
        placa.append(random.nextInt(10));
        placa.append(random.nextInt(10));
        return placa.toString();
    }

    protected static boolean validarPlaca(String placa){
        return placa.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    }
}
