package caminhoes;

public class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    private static final int[] OPCOES = {2000, 4000, 8000, 10000};
    private static String id;

    public CaminhaoPequeno(int escolha, String placaOpcional) {
        this.cargaAtual = 0;
        if (escolha >= 1 && escolha <= 4) {
            this.capacidade = OPCOES[escolha - 1];
        } else {
            throw new IllegalArgumentException("Escolha deve ser de 1 a 4.");
        }
        if (placaOpcional != null && !placaOpcional.isBlank() && Placa.validarPlaca(placaOpcional)) {
            CaminhaoPequeno.id = placaOpcional.toUpperCase();
            //O código trata placas inválidas do mesmo jeito que nulas, ou seja, só gera uma placa aleatória no lugar
        } else {
            CaminhaoPequeno.id = Placa.gerarPlaca();
        }
    }

    public CaminhaoPequeno(int escolha) {
        this(escolha, null);
    }

    public String getPlaca() {
        return id;
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