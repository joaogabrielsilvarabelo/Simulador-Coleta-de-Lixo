package caminhoes;

public class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    private static final int[] OPCOES = {2000, 4000, 8000, 10000};
    private static String id;

    public CaminhaoPequeno(int escolha, String placaOpcional) {
        this.cargaAtual = 0;
        this.capacidade = determinarCapacidade(escolha);
        CaminhaoPequeno.id = processarPlaca(placaOpcional);
    }

    private int determinarCapacidade(int escolha) {
        if (escolha < 1 || escolha > 4) {
            throw new IllegalArgumentException("Escolha deve ser de 1 a 4.");
        }
        return OPCOES[escolha - 1];
    }

    private static String processarPlaca(String placaOpcional) {
        if (placaOpcional != null) {
            if (!Placa.validarPlaca(placaOpcional)) {
                throw new IllegalArgumentException("Placa não segue normas do Mercosul");
            }
            if (!placaOpcional.isBlank()) {
                return placaOpcional.toUpperCase();
            }
        }
        return Placa.gerarPlaca();
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