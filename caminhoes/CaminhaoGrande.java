package caminhoes;

public class CaminhaoGrande {
    protected int capacidade = 20000;
    protected int cargaAtual;
    private static String id;
    private int tempoTolerancia = 200;

    public CaminhaoGrande(String placaOpcional) {
        this.cargaAtual = 0;
        CaminhaoGrande.id = processarPlaca(placaOpcional);
    }

    public CaminhaoGrande() {
        this(null);
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

    public void carregar(int quantidade) {
        cargaAtual += quantidade;
        if (cargaAtual > capacidade) {
            cargaAtual = capacidade;
        }
    }

    public boolean prontoParaPartir() {
        return cargaAtual >= capacidade;
    }

    public void descarregar() {
        System.out.println("Caminhão grande partiu para o aterro com " + cargaAtual + "kg.");
        cargaAtual = 0;
    }

    public String getPlaca() {
        return id;
    }

    public int getCargaAtual() {
        return cargaAtual;
    }

    public int getCapacidade() {
        return capacidade;
    }
}