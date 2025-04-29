package caminhoes;


public class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    private static final int[] OPCOES = {2000, 4000, 8000, 10000};

    public CaminhaoPequeno(int escolha) {
        this.cargaAtual = 0;
        if (escolha >= 1 && escolha <= 4) {
            this.capacidade = OPCOES[escolha - 1];
        } else {
            throw new IllegalArgumentException("Escolha deve ser de 1 a 4.");
        }
    }

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

    public boolean estaCheio() {
        return cargaAtual >= capacidade;
    }

    public int descarregar() {
        int carga = cargaAtual;
        cargaAtual = 0;
        return carga;
    }

    public int getCargaAtual() {
        return cargaAtual;
    }
}