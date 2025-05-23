package caminhoes;

import estacoes.EstacaoTransferencia;
import simulacao.Simulador;
import simulacao.LoggerSimulacao;
import zonas.ZonaUrbana;

public class CaminhaoPequeno {
    private final int capacidade;
    public int cargaAtual;
    private final String id;
    private final int limiteViagens;
    public int viagensFeitas;
    private int status;
    private ZonaUrbana zonaAtual;
    public ZonaUrbana zonaDestino;
    private final ZonaUrbana zonaInicial;
    private EstacaoTransferencia estacaoDestino;
    private int tempoViagemRestante;
    private static final int[] OPCOES = {2000, 4000, 8000, 10000};
    private static final int TEMPO_COLETA_POR_KG = 10; // X minutos por 1000kg, ajustável
    private int tempoColetaRestante;
    private int tempoEsperaFila;
    private int quantidadeColetando; // Quantidade total a ser coletada
    private int cargaPorMinuto; // Carga a ser adicionada por minuto durante a coleta
    private int tempoDescarregamentoRestante; // Tempo restante para descarregamento
    private int quantidadeDescarregando; // Quantidade total a ser descarregada
    private int descargaPorMinuto; // Carga a ser descarregada por minuto

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
        this.quantidadeColetando = 0;
        this.cargaPorMinuto = 0;
        this.tempoDescarregamentoRestante = 0;
        this.quantidadeDescarregando = 0;
        this.descargaPorMinuto = 0;
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

    // Inicia coleta de lixo, definindo quantidade e tempo
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
            quantidadeColetando = coletado; // Armazena quantidade total a ser coletada
            // Calcula tempo de coleta: X minutos por 1000kg (ajustável)
            tempoColetaRestante = (int) Math.ceil(coletado / 1000.0 * TEMPO_COLETA_POR_KG);
            // Calcula carga por minuto (distribui a carga ao longo do tempo de coleta)
            cargaPorMinuto = (int) Math.ceil((double) coletado / tempoColetaRestante);
            setEstado(2); // COLETANDO
            LoggerSimulacao.log("COLETA", "Caminhão " + id + " iniciou coleta de " + coletado + "kg em " + zonaAtual.getNome() +
                    ", tempo estimado: " + tempoColetaRestante + "min, carga por minuto: " + cargaPorMinuto + "kg.");
        } else {
            LoggerSimulacao.log("INFO", "Caminhão " + id + " não coletou (sem lixo suficiente ou cheio).");
        }
        return coletado;
    }

    // Processa coleta incremental, adicionando carga por minuto
    public boolean processarColeta() {
        if (tempoColetaRestante > 0) {
            tempoColetaRestante--;
            // Calcula carga a ser adicionada neste minuto (limitada pela quantidade restante)
            int cargaAdicionar = Math.min(cargaPorMinuto, quantidadeColetando);
            cargaAtual += cargaAdicionar;
            quantidadeColetando -= cargaAdicionar;
            // Atualiza a zona com a quantidade coletada e registra nas estatísticas
            if (zonaAtual != null) {
                zonaAtual.coletarLixo(cargaAdicionar);
                Simulador.getEstatisticas().registrarColeta(cargaAdicionar, zonaAtual.getNome());
            }
            // Verifica se a coleta foi finalizada
            if (tempoColetaRestante == 0) {
                // Se ainda restar alguma carga não adicionada (por arredondamentos), coleta agora
                if (quantidadeColetando > 0) {
                    cargaAtual += quantidadeColetando;
                    if (zonaAtual != null) {
                        zonaAtual.coletarLixo(quantidadeColetando);
                        Simulador.getEstatisticas().registrarColeta(quantidadeColetando, zonaAtual.getNome());
                    }
                    quantidadeColetando = 0;
                }
                // Registra um único log consolidado ao final da coleta
                int tempoTotalColeta = (int) Math.ceil((double) cargaAtual / (cargaPorMinuto > 0 ? cargaPorMinuto : 1));
                String nomeZona = zonaAtual != null ? zonaAtual.getNome() : "Desconhecida";
                LoggerSimulacao.log("COLETA", String.format(
                        "Caminhão %s finalizou coleta de %dkg em %s. Tempo de coleta: %dmin. Carga final: %dkg.",
                        id, cargaAtual, nomeZona, tempoTotalColeta, cargaAtual));
                // Limpa o indicador de carga por minuto (fim da coleta)
                cargaPorMinuto = 0;
            }
            return tempoColetaRestante == 0;
        }
        return true;
    }

    // Inicia descarregamento, definindo quantidade e tempo
    public void iniciarDescarregamento(int quantidade, int tempoPor1000kg) {
        if (getEstado() == 6) {
            LoggerSimulacao.log("ERRO", "Caminhão " + id + " está ENCERRADO e não pode descarregar.");
            return;
        }
        if (quantidade > cargaAtual) {
            LoggerSimulacao.log("ERRO", "Quantidade a descarregar (" + quantidade + "kg) excede carga atual (" + cargaAtual + "kg).");
            return;
        }
        quantidadeDescarregando = quantidade;
        tempoDescarregamentoRestante = (int) Math.ceil(quantidade / 1000.0 * tempoPor1000kg);
        descargaPorMinuto = (int) Math.ceil((double) quantidade / tempoDescarregamentoRestante);
        LoggerSimulacao.log("INFO", String.format("Caminhão %s iniciou descarregamento de %dkg, tempo estimado: %dmin, descarga por minuto: %dkg.",
                id, quantidade, tempoDescarregamentoRestante, descargaPorMinuto));
    }

    // Processa descarregamento incremental, transferindo carga para o caminhão grande
    public boolean processarDescarregamento(CaminhaoGrande caminhaoGrande) {
        if (tempoDescarregamentoRestante > 0) {
            tempoDescarregamentoRestante--;
            // Calcula carga a ser descarregada neste minuto
            int cargaDescarregar = Math.min(descargaPorMinuto, quantidadeDescarregando);
            if (caminhaoGrande != null) {
                caminhaoGrande.carregar(cargaDescarregar);
                cargaAtual -= cargaDescarregar;
                quantidadeDescarregando -= cargaDescarregar;
            }
            // Verifica se o descarregamento foi finalizado
            if (tempoDescarregamentoRestante == 0) {
                // Descarrega qualquer carga restante devido a arredondamentos
                if (quantidadeDescarregando > 0 && caminhaoGrande != null) {
                    caminhaoGrande.carregar(quantidadeDescarregando);
                    cargaAtual -= quantidadeDescarregando;
                    quantidadeDescarregando = 0;
                }
                descargaPorMinuto = 0;
                //Limpa a estação destino depois do descarregamento
                setEstacaoDestino(null);
                return true;
            }
            return false;
        }
        return true;
    }

    // Verifica se o limite de viagens foi atingido
    public void isLimiteAtingido(int viagensFeitas) {
        if (viagensFeitas >= limiteViagens) {
            setEstado(6); // ENCERRADO
            if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
                LoggerSimulacao.log("INFO", String.format("Caminhão %s atingiu o limite de %d viagens diárias e foi encerrado.", id, limiteViagens));
            }
        }
    }

    // Descarrega toda a carga do caminhão
    public int descarregar() {
        int carga = cargaAtual;
        cargaAtual = 0;
        return carga;
    }

    // Define o tempo de viagem, se não encerrado
    public void definirTempoViagem(int minutos) {
        if (getEstado() == 6) {
            LoggerSimulacao.log("ERRO", "Caminhão " + id + " está ENCERRADO e não pode viajar.");
            return;
        }
        this.tempoViagemRestante = minutos;
    }

    // Processa a viagem, retornando true se concluída
    public boolean processarViagem() {
        if (tempoViagemRestante > 0) {
            tempoViagemRestante--;
            return tempoViagemRestante == 0;
        }
        return true;
    }

    // Verifica se o caminhão está cheio
    public boolean estaCheio() {
        return cargaAtual >= capacidade;
    }

    // Getters e Setters
    public int getEstado() { return status; }
    public void setEstado(int status) { this.status = status; }
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
    public String getPlaca() { return id; }
    public int getCapacidade() { return capacidade; }
    public int getViagensFeitas() { return viagensFeitas; }
    public int getCargaAtual() { return cargaAtual; }
    public ZonaUrbana getZonaAtual() { return zonaAtual; }
    public void setZonaAtual(ZonaUrbana zona) { this.zonaAtual = zona; }
    public ZonaUrbana getZonaInicial() { return zonaInicial; }
    public EstacaoTransferencia getEstacaoDestino() { return estacaoDestino; }
    public void setEstacaoDestino(EstacaoTransferencia estacao) { this.estacaoDestino = estacao; }
    public ZonaUrbana getZonaDestino() { return zonaDestino; }
    public void setZonaDestino(ZonaUrbana zona) { this.zonaDestino = zona; }
    public int getTempoColetaRestante() { return tempoColetaRestante; }
    public int getTempoEsperaFila() { return tempoEsperaFila; }
    public void incrementarTempoEspera() { this.tempoEsperaFila++; }
    public void resetarTempoEspera() { this.tempoEsperaFila = 0; }
    public int getTempoViagemRestante() { return tempoViagemRestante; }
    public int getTempoDescarregamentoRestante() { return tempoDescarregamentoRestante; }
}


