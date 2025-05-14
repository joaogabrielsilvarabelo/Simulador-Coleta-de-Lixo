package caminhoes;

import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;

public class CaminhaoPequeno {
    private final int capacidade;
    private int cargaAtual;
    private final String id;
    private final int limiteViagens;
    public int viagensFeitas;
    private int status;
    private ZonaUrbana zonaAtual;
    private final ZonaUrbana zonaInicial;
    private EstacaoTransferencia estacaoDestino;
    private int tempoViagemRestante;
    private ZonaUrbana zonaDestino;
    private static final int[] OPCOES = {2000, 4000, 8000, 10000};

    public CaminhaoPequeno(int escolha, int maxViagens, ZonaUrbana zonaInicial, String placaOpcional) {
        this.cargaAtual = 0;
        this.capacidade = determinarCapacidade(escolha);
        this.id = Placa.processarPlaca(placaOpcional);
        this.limiteViagens = maxViagens;
        this.viagensFeitas = 0;
        this.status = 1; // DISPONÍVEL
        this.zonaInicial = zonaInicial;
        this.zonaAtual = zonaInicial;
        this.estacaoDestino = null;
        this.tempoViagemRestante = 0;
    }

    public CaminhaoPequeno(int escolha, int maxViagens, ZonaUrbana zonaInicial) {
        this(escolha, maxViagens, zonaInicial, null);
    }

    private int determinarCapacidade(int escolha) {
        if (escolha < 1 || escolha > 4) {
            throw new IllegalArgumentException("Escolha deve ser de 1 a 4.");
        }
        return OPCOES[escolha - 1];
    }

    public int getEstado() {
        return status;
    }

    public void setEstado(int status) {
        this.status = status;
    }

    public String determinarEstado(int status) {
        return switch (status) {
            case 1 -> "DISPONÍVEL";
            case 2 -> "COLETANDO";
            case 3 -> "EM_TRÂNSITO";
            case 4 -> "FILA_ESTAÇÃO";
            case 5 -> "DESCARREGANDO";
            case 6 -> "ENCERRADO";
            default -> "DESCONHECIDO";
        };
    }

    public String getPlaca() {
        return id;
    }

    public boolean limiteAtingido(int viagensFeitas) {
        return viagensFeitas >= limiteViagens;
    }

    public void setZonaAtual(ZonaUrbana zona) {
        this.zonaAtual = zona;
    }

    public ZonaUrbana getZonaAtual() {
        return zonaAtual;
    }

    public ZonaUrbana getzonaInicial() {
        return zonaInicial;
    }

    public int coletar(int quantidade) {
        int espacoLivre = capacidade - cargaAtual;
        int coletado = Math.min(quantidade, espacoLivre);
        cargaAtual += coletado;
        return coletado;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getViagensFeitas() {
        return viagensFeitas;
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

    public void definirTempoViagem(int minutos) {
        this.tempoViagemRestante = minutos;
    }

    public void setEstacaoDestino(EstacaoTransferencia estacao) {
        this.estacaoDestino = estacao;
    }

    public EstacaoTransferencia getEstacaoDestino() {
        return estacaoDestino;
    }

    public void setZonaDestino(ZonaUrbana zona) {
        this.zonaDestino = zona;
    }

    public ZonaUrbana getZonaDestino() {
        return zonaDestino;
    }

    public boolean processarViagem() {
        if (tempoViagemRestante > 0) {
            tempoViagemRestante--;
            return tempoViagemRestante == 0;
        }
        return true;
    }
}