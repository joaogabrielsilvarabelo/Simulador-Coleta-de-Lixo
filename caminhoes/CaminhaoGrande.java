package caminhoes;

public class CaminhaoGrande {
    private final int capacidade = 20000;
    private int cargaAtual;
    private final String id;
    private final int tempoTolerancia;

    public CaminhaoGrande(String placaOpcional, int tempoTolerancia) {
        this.cargaAtual = 0;
        this.tempoTolerancia = tempoTolerancia;
        this.id = Placa.processarPlaca(placaOpcional);
    }

    public CaminhaoGrande() {
        this(null, 200);
    }

    public void carregar(int quantidade) {
        cargaAtual = Math.min(cargaAtual + quantidade, capacidade);
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

    public int getTempoTolerancia() {
        return tempoTolerancia;
    }
}