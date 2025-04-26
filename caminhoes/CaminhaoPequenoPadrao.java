package caminhoes;

import java.util.Random;

public class CaminhaoPequenoPadrao extends CaminhaoPequeno {

    public CaminhaoPequenoPadrao() {
        this.cargaAtual = 0;
        int[] opcoes = {2000, 4000, 8000, 10000};
        Random random = new Random();
        int indice = random.nextInt(opcoes.length);
        this.capacidade = opcoes[indice];
    }

    @Override
    public boolean coletar(int quantidade) {
        if (cargaAtual + quantidade <= capacidade) {
            cargaAtual += quantidade;
            return true;
        }
        return false;
    }

    public int getCapacidade() {
        return capacidade;
    }
}