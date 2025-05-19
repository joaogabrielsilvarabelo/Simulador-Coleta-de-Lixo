package caminhoes;

public class CaminhaoGrande {
    private static final int TEMPO_DESCARREGAMENTO = 5;
    private final int capacidade = 20000;
    private int cargaAtual;
    private final String id;
    private final int tempoTolerancia;
    private int status; // 1 = ESPERANDO, 2 = EM_TRANSITO, 3 = RECEBENDO_LIXO

    public CaminhaoGrande(String placaOpcional, int tempoTolerancia) {
        this.cargaAtual = 0;
        this.tempoTolerancia = tempoTolerancia;
        this.status = 1; // ESPERANDO
        this.id = Placa.processarPlaca(placaOpcional);
    }

    public CaminhaoGrande() {
        this(null, 200);
    }

    public void carregar(int quantidade) {
        cargaAtual = Math.min(cargaAtual + quantidade, capacidade);
    }

    // Descarrega o lixo do caminhão grande e registra no aterro
    public void descarregar() {
        // Registra a quantidade de lixo enviada ao aterro nas estatísticas
        simulacao.Simulador.getEstatisticas().registrarLixoAterro(cargaAtual);
        cargaAtual = 0;
    }

    public String determinarEstado(int status) {
        return switch (status) {
            case 1 -> "ESPERANDO";
            case 2 -> "EM_TRÂNSITO";
            case 3 -> "RECEBENDO_LIXO";
            default -> "DESCONHECIDO";
        };
    }

    public int getEstado() {
        return status;
    }

    public void setEstado(int status) {
        this.status = status;
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

    public int getToleranciaEspera() {
        return tempoTolerancia;
    }
}
