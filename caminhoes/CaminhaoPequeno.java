package caminhoes;

import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import simulacao.LoggerSimulacao;

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
    private static final int TEMPO_COLETA_POR_KG = 10; // X minutos por 1000kg, ajustável
    private int tempoColetaRestante;
    private int tempoEsperaFila;

    public CaminhaoPequeno(int escolha, int limiteViagens, ZonaUrbana zonaInicial, String placaOpcional) {
        this.cargaAtual = 0;
        this.capacidade = determinarCapacidade(escolha);
        this.id = Placa.processarPlaca(placaOpcional);
        this.limiteViagens = limiteViagens;
        this.viagensFeitas = 0;
        this.status = 1; // DISPONÍVEL
        this.zonaInicial = zonaInicial;
        this.zonaAtual = zonaInicial;
        this.estacaoDestino = null;
        this.tempoViagemRestante = 0;
        this.tempoEsperaFila = 0;
    }

    public CaminhaoPequeno(int escolha, int limiteViagens, ZonaUrbana zonaInicial) {
        this(escolha, limiteViagens, zonaInicial, null);
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

    public void isLimiteAtingido(int viagensFeitas) {
        if (viagensFeitas >= limiteViagens) {
            setEstado(6);
        }
    }

    public void setZonaAtual(ZonaUrbana zona) {
        this.zonaAtual = zona;
    }

    public ZonaUrbana getZonaAtual() {
        return zonaAtual;
    }

    public ZonaUrbana getZonaInicial() {
        return zonaInicial;
    }

    public int coletar(int quantidade) {
        if (getEstado() == 6) {
            LoggerSimulacao.log("ERRO", "Caminhão " + id + " está ENCERRADO e não pode coletar.");
            return 0;
        }
        if (tempoColetaRestante > 0) {
            LoggerSimulacao.log("INFO", "Caminhão " + id + " ainda está coletando, tempo restante: " + tempoColetaRestante + "min.");
            return 0;
        }
        int espacoLivre = capacidade - cargaAtual;
        // Permite coleta parcial com base no lixo disponível
        int coletado = Math.min(quantidade, espacoLivre);
        if (coletado > 0) {
            cargaAtual += coletado;
            // Calcula tempo de coleta: X minutos por 1000kg (ajustável)
            tempoColetaRestante = (int) Math.ceil(coletado / 1000.0 * TEMPO_COLETA_POR_KG);
            setEstado(2); // COLETANDO
            LoggerSimulacao.log("COLETA", "Caminhão " + id + " iniciou coleta de " + coletado + "kg, tempo estimado: " + tempoColetaRestante + "min.");
        } else {
            LoggerSimulacao.log("INFO", "Caminhão " + id + " não coletou (sem lixo suficiente ou cheio).");
        }
        return coletado;
    }

    public boolean processarColeta() {
        if (tempoColetaRestante > 0) {
            tempoColetaRestante--;
            return tempoColetaRestante == 0;
        }
        return true;
    }

    public int getTempoColetaRestante() {
        return tempoColetaRestante;
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
        if (getEstado() == 6) {
            LoggerSimulacao.log("ERRO", "Caminhão " + id + " está ENCERRADO e não pode viajar.");
            return;
        }
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

    // Incrementa o tempo de espera na fila
    public void incrementarTempoEspera() {
        this.tempoEsperaFila++;
    }

    // Obtém o tempo de espera na fila
    public int getTempoEsperaFila() {
        return tempoEsperaFila;
    }

    // Reseta o tempo de espera após descarregamento
    public void resetarTempoEspera() {
        this.tempoEsperaFila = 0;
    }

    public boolean processarViagem() {
        if (tempoViagemRestante > 0) {
            tempoViagemRestante--;
            return tempoViagemRestante == 0;
        }
        return true;
    }

    public int getTempoViagemRestante() {
        return tempoViagemRestante;
    }
}
