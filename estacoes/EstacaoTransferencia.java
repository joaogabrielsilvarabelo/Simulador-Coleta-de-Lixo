package estacoes;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import caminhoes.DistribuicaoCaminhoes;
import estruturas.Fila;
import estruturas.Lista;
import simulacao.LoggerSimulacao;
import simulacao.Simulador;
import zonas.ZonaUrbana;

public class EstacaoTransferencia {
    private final String nome;
    public int lixoArmazenado;
    private final Fila<CaminhaoPequeno> filaPequenos;
    private final Lista<CaminhaoGrande> listaGrandes;
    private final int esperaMaxPequenos;
    private int esperaTotalPequenos;
    public CaminhaoGrande caminhaoGrandeEsperando;
    private ZonaUrbana zonaDaEstacao;
    public boolean temCaminhaoGrandeEsperando;
    public int tempoEsperaCaminhaoGrande;
    private int tempoProcessamentoRestante;
    private boolean processandoGrande;
    private int cargaParaGrande;
    private static final int TEMPO_PROCESSAMENTO_PEQUENO = 5; // Tempo para descarregar caminhão pequeno (minutos)
    private static final int TEMPO_TRANSFERENCIA_GRANDE = 7; // Tempo para transferir lixo a caminhão grande (minutos)
    private static final int CAPACIDADE_ARMAZENAMENTO = 100000; // 100 toneladas

    public EstacaoTransferencia(String nome, int esperaMaxPequenos, ZonaUrbana zonaDaEstacao) {
        this.nome = nome;
        this.lixoArmazenado = 0;
        this.filaPequenos = new Fila<>();
        this.listaGrandes = new Lista<>();
        this.esperaMaxPequenos = esperaMaxPequenos;
        this.esperaTotalPequenos = 0;
        this.caminhaoGrandeEsperando = null;
        this.zonaDaEstacao = zonaDaEstacao;
        this.temCaminhaoGrandeEsperando = false;
        this.tempoEsperaCaminhaoGrande = 0;
        this.tempoProcessamentoRestante = 0;
        this.processandoGrande = false;
        this.cargaParaGrande = 0;
    }
    // Recebe um caminhão pequeno e o adiciona à fila
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao, int tempoAtual) {
        if (lixoArmazenado + caminhao.getCargaAtual() > CAPACIDADE_ARMAZENAMENTO) {
            LoggerSimulacao.log("ERRO", String.format("%s: Capacidade de armazenamento excedida. Caminhão %s redirecionado.", nome, caminhao.getPlaca()));
            // Tenta redirecionar para outra estação
            EstacaoTransferencia outraEstacao = escolherOutraEstacao();
            if (outraEstacao != null) {
                int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(caminhao.getZonaAtual(), outraEstacao.getZonaDaEstacao());
                caminhao.definirTempoViagem(tempoViagem);
                caminhao.setEstacaoDestino(outraEstacao);
                LoggerSimulacao.log("INFO", String.format("Caminhão %s redirecionado para %s (viagem: %dmin)",
                        caminhao.getPlaca(), outraEstacao.getNome(), tempoViagem));
            }
            return;
        }
        filaPequenos.enfileirar(caminhao);
        caminhao.incrementarTempoEspera(); // Inicia contagem de espera
        LoggerSimulacao.log("CHEGADA", String.format("%s: Caminhão %s entrou na fila. Tamanho da fila: %d", nome, caminhao.getPlaca(), filaPequenos.getTamanho()));
    }
    // Escolhe outra estação com capacidade disponível
    private EstacaoTransferencia escolherOutraEstacao() {
        for (int i = 0; i < Simulador.getEstacoes().getTamanho(); i++) {
            EstacaoTransferencia estacao = Simulador.getEstacoes().obter(i);
            if (!estacao.getNome().equals(this.nome) && estacao.lixoArmazenado < CAPACIDADE_ARMAZENAMENTO) {
                return estacao;
            }
        }
        return null;
    }
    // Processa a fila de caminhões pequenos e transferências para caminhões grandes
    public ResultadoProcessamentoFila processarFila(int tempoAtual) {
        // Atualiza tempo de espera de todos os caminhões na fila
        for (int i = 0; i < filaPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = filaPequenos.obter(i);
            caminhao.incrementarTempoEspera();
        }
        // Se não há caminhões pequenos na fila nem lixo armazenado, reseta estados
        if (filaPequenos.estaVazia() && lixoArmazenado == 0) {
            tempoProcessamentoRestante = 0;
            processandoGrande = false;
            cargaParaGrande = 0;
            return new ResultadoProcessamentoFila(null, 0, false);
        }
        // Se há processamento em andamento, continua a contagem regressiva
        if (tempoProcessamentoRestante > 0) {
            tempoProcessamentoRestante--;
            if (tempoProcessamentoRestante == 0) {
                // Finaliza a transferência para o caminhão grande
                if (processandoGrande && caminhaoGrandeEsperando != null) {
                    caminhaoGrandeEsperando.carregar(cargaParaGrande);
                    lixoArmazenado -= cargaParaGrande;
                    LoggerSimulacao.log("DESCARGA", String.format("%s: Transferiu %dkg para caminhão grande %s. Carga atual: %d/%dkg. Lixo restante: %dkg",
                            nome, cargaParaGrande, caminhaoGrandeEsperando.getPlaca(),
                            caminhaoGrandeEsperando.getCargaAtual(), caminhaoGrandeEsperando.getCapacidade(), lixoArmazenado));
                    cargaParaGrande = 0;
                    processandoGrande = false;
                }
            }
            return new ResultadoProcessamentoFila(null, 0, false);
        }
        // Prioriza descarregamento de caminhões pequenos se há fila
        if (!filaPequenos.estaVazia()) {
            CaminhaoPequeno caminhaoPequeno = filaPequenos.primeiroDaFila();
            // Se há caminhão grande disponível e espaço suficiente, descarrega diretamente
            if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null &&
                    caminhaoGrandeEsperando.getCargaAtual() + caminhaoPequeno.getCargaAtual() <= caminhaoGrandeEsperando.getCapacidade()) {
                caminhaoPequeno = filaPequenos.remover();
                int cargaDescarregada = caminhaoPequeno.descarregar();
                caminhaoGrandeEsperando.carregar(cargaDescarregada);
                LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s descarregou %dkg diretamente no caminhão grande %s",
                        nome, caminhaoPequeno.getPlaca(), cargaDescarregada, caminhaoGrandeEsperando.getPlaca()));
                int tempoEspera = caminhaoPequeno.getTempoEsperaFila();
                caminhaoPequeno.resetarTempoEspera();
                tempoProcessamentoRestante = TEMPO_PROCESSAMENTO_PEQUENO;
                processandoGrande = false;
                return new ResultadoProcessamentoFila(caminhaoPequeno, tempoEspera, true);
            } else {
                // Descarrega no armazenamento da estação
                caminhaoPequeno = filaPequenos.remover();
                int cargaDescarregada = caminhaoPequeno.descarregar();
                lixoArmazenado += cargaDescarregada;
                LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s descarregou %dkg no armazenamento. Lixo armazenado: %dkg",
                        nome, caminhaoPequeno.getPlaca(), cargaDescarregada, lixoArmazenado));
                int tempoEspera = caminhaoPequeno.getTempoEsperaFila();
                caminhaoPequeno.resetarTempoEspera();
                tempoProcessamentoRestante = TEMPO_PROCESSAMENTO_PEQUENO;
                processandoGrande = false;
                return new ResultadoProcessamentoFila(caminhaoPequeno, tempoEspera, true);
            }
        }
        // Processa transferência de lixo armazenado para caminhão grande, se aplicável
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null && lixoArmazenado > 0 && !processandoGrande) {
            int cargaTransferir = Math.min(lixoArmazenado, caminhaoGrandeEsperando.getCapacidade() - caminhaoGrandeEsperando.getCargaAtual());
            if (cargaTransferir > 0) {
                cargaParaGrande = cargaTransferir;
                tempoProcessamentoRestante = TEMPO_TRANSFERENCIA_GRANDE;
                processandoGrande = true;
                LoggerSimulacao.log("INFO", String.format("%s: Iniciando transferência de %dkg para caminhão grande %s",
                        nome, cargaTransferir, caminhaoGrandeEsperando.getPlaca()));
            }
        }
        return new ResultadoProcessamentoFila(null, 0, false);
    }
    // Atribui um caminhão grande à estação
    public void atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        if (!temCaminhaoGrandeEsperando) {
            caminhaoGrandeEsperando = caminhao;
            temCaminhaoGrandeEsperando = true;
            tempoEsperaCaminhaoGrande = 0;
            listaGrandes.adicionar(caminhao);
            LoggerSimulacao.log("INFO", String.format("%s: Caminhão grande %s atribuído", nome, caminhao.getPlaca()));
        } else {
            LoggerSimulacao.log("ERRO", String.format("%s: Já existe um caminhão grande (%s) atribuído", nome, caminhaoGrandeEsperando.getPlaca()));
        }
    }
    // Verifica se o tempo máximo de espera para caminhões pequenos foi excedido
    public boolean tempoEsperaExcedido() {
        return esperaTotalPequenos > esperaMaxPequenos && !filaPequenos.estaVazia();
    }
    // Atualiza o tempo de espera do caminhão grande
    public void atualizarTempoEsperaCaminhaoGrande() {
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null) {
            tempoEsperaCaminhaoGrande++;
            if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
                LoggerSimulacao.log("INFO", String.format("%s: Caminhão grande %s esperando há %d minutos",
                        nome, caminhaoGrandeEsperando.getPlaca(), tempoEsperaCaminhaoGrande));
            }
        }
    }
    // Libera o caminhão grande se a tolerância de espera for excedida
    public CaminhaoGrande liberarCaminhaoGrandeSeNecessario() {
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null &&
                tempoEsperaCaminhaoGrande >= caminhaoGrandeEsperando.getToleranciaEspera() &&
                caminhaoGrandeEsperando.getCargaAtual() > 0) {
            CaminhaoGrande liberado = caminhaoGrandeEsperando;
            Simulador.getEstatisticas().registrarLixoAterro(liberado.getCargaAtual()); // Registrar lixo no aterro
            caminhaoGrandeEsperando = null;
            temCaminhaoGrandeEsperando = false;
            tempoEsperaCaminhaoGrande = 0;
            LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão grande %s liberado para o aterro com %dkg (tolerância excedida)",
                    nome, liberado.getPlaca(), liberado.getCargaAtual()));
            return liberado;
        }
        return null;
    }
    // Getters
    public String getNome() {
        return nome;
    }

    public Fila<CaminhaoPequeno> getFilaPequenos() {
        return filaPequenos;
    }

    public ZonaUrbana getZonaDaEstacao() {
        return zonaDaEstacao;
    }

    public boolean temCaminhaoGrandeFila() {
        return temCaminhaoGrandeEsperando;
    }
}
