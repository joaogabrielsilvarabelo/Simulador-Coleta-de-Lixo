package estacoes;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.DistribuicaoCaminhoes;
import estruturas.Fila;
import estruturas.Lista;
import simulacao.LoggerSimulacao;
import simulacao.Simulador;
import zonas.ZonaUrbana;

public class EstacaoTransferencia {
    private final String nome;
    private final int esperaMaxPequenos;
    private int esperaTotalPequenos;
    private final Fila<CaminhaoPequeno> filaPequenos;
    private final Lista<CaminhaoGrande> listaGrandes;
    public CaminhaoGrande caminhaoGrandeEsperando;
    private boolean temCaminhaoGrandeEsperando;
    private int tempoEsperaCaminhaoGrande;
    private final ZonaUrbana zonaDaEstacao;
    private static final int TEMPO_POR_2000KG = 1; // 1 minutos por 2000kg
    private CaminhaoPequeno caminhaoPequenoDescarregando; // Referência ao caminhão pequeno descarregando

    public EstacaoTransferencia(String nome, int esperaMaxPequenos, ZonaUrbana zona) {
        this.nome = nome;
        this.esperaMaxPequenos = esperaMaxPequenos;
        this.esperaTotalPequenos = 0;
        this.filaPequenos = new Fila<>();
        this.listaGrandes = new Lista<>();
        this.caminhaoGrandeEsperando = null;
        this.temCaminhaoGrandeEsperando = false;
        this.tempoEsperaCaminhaoGrande = 0;
        this.zonaDaEstacao = zona;
        this.caminhaoPequenoDescarregando = null;
    }

    // Recebe um caminhão pequeno, adicionando-o à fila
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao, int tempoSimulado) {
        if (caminhao.getEstado() == 6) {
            LoggerSimulacao.log("ERRO", String.format("%s: Caminhão %s está ENCERRADO e não pode ser adicionado à fila.", nome, caminhao.getPlaca()));
            return;
        }
        caminhao.setEstado(4); // FILA_ESTAÇÃO
        caminhao.resetarTempoEspera();
        filaPequenos.enfileirar(caminhao);
        esperaTotalPequenos += caminhao.getTempoEsperaFila();
        LoggerSimulacao.log("CHEGADA", String.format("%s: Caminhão %s chegou na estação e entrou na fila com %dkg. Tamanho da fila: %d", nome, caminhao.getPlaca(), caminhao.getCargaAtual(), filaPequenos.getTamanho()));
    }

    // Processa a fila de caminhões pequenos
    public ResultadoProcessamentoFila processarFila(int tempoSimulado) {
        // Verifica se há condições para processar a fila
        if (filaPequenos.estaVazia() || !temCaminhaoGrandeEsperando || caminhaoGrandeEsperando == null) {
            return new ResultadoProcessamentoFila(null, 0, false);
        }
        // Incrementa o tempo de espera dos caminhões na fila
        incrementarTempoEsperaFila();
        // Processa descarregamento em andamento, se houver
        if (caminhaoPequenoDescarregando != null && caminhaoPequenoDescarregando.getEstado() == 5) {
            return processarDescarregamentoEmAndamento();
        }
        // Inicia descarregamento de um novo caminhão pequeno
        return iniciarNovoDescarregamento();
    }

    // Incrementa o tempo de espera para todos os caminhões na fila
    private void incrementarTempoEsperaFila() {
        for (int i = 0; i < filaPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = filaPequenos.obter(i);
            caminhao.incrementarTempoEspera();
            esperaTotalPequenos++;
        }
    }

    // Processa o descarregamento de um caminhão pequeno em andamento
    private ResultadoProcessamentoFila processarDescarregamentoEmAndamento() {
        boolean descarregamentoConcluido = caminhaoPequenoDescarregando.processarDescarregamento(caminhaoGrandeEsperando);
        if (descarregamentoConcluido) {
            LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s terminou de descarregar.", nome, caminhaoPequenoDescarregando.getPlaca()));
            int tempoEspera = caminhaoPequenoDescarregando.getTempoEsperaFila();
            CaminhaoPequeno caminhaoProcessado = caminhaoPequenoDescarregando;
            caminhaoPequenoDescarregando = null; // Libera a referência
            caminhaoProcessado.setEstado(1); // DISPONÍVEL
            LoggerSimulacao.log("INFO", String.format("%s: Caminhão %s agora disponível para redistribuição após descarregar.", nome, caminhaoProcessado.getPlaca()));
            // Tenta iniciar o próximo descarregamento imediatamente
            ResultadoProcessamentoFila resultadoProximo = iniciarNovoDescarregamento();
            if (resultadoProximo.foiProcessado()) {
                return resultadoProximo; // Retorna o próximo caminhão processado, se concluído
            }
            return new ResultadoProcessamentoFila(caminhaoProcessado, tempoEspera, true);
        }
        return new ResultadoProcessamentoFila(null, 0, false);
    }

    // Inicia o descarregamento de um novo caminhão pequeno
    private ResultadoProcessamentoFila iniciarNovoDescarregamento() {
        CaminhaoPequeno caminhao = filaPequenos.remover();
        if (caminhao == null) {
            return new ResultadoProcessamentoFila(null, 0, false);
        }
        caminhaoPequenoDescarregando = caminhao;
        caminhao.setEstado(5); // DESCARREGANDO
        caminhao.iniciarDescarregamento(caminhao.getCargaAtual(), TEMPO_POR_2000KG);
        LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s iniciou descarregamento de %dkg.", nome, caminhao.getPlaca(), caminhao.getCargaAtual()));
        boolean descarregamentoConcluido = caminhao.processarDescarregamento(caminhaoGrandeEsperando);
        if (descarregamentoConcluido) {
            LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s terminou de descarregar.", nome, caminhao.getPlaca()));
            int tempoEspera = caminhao.getTempoEsperaFila();
            caminhao.setEstado(1); // DISPONÍVEL
            LoggerSimulacao.log("INFO", String.format("%s: Caminhão %s agora disponível para redistribuição após descarregar.", nome, caminhao.getPlaca()));
            caminhaoPequenoDescarregando = null;
            // Tenta iniciar o próximo descarregamento apenas se a fila não está vazia
            if (!filaPequenos.estaVazia()) {
                ResultadoProcessamentoFila resultadoProximo = iniciarNovoDescarregamento();
                if (resultadoProximo.foiProcessado()) {
                    return resultadoProximo; // Retorna o próximo caminhão processado, se concluído
                }
            }
            return new ResultadoProcessamentoFila(caminhao, tempoEspera, true);
        }
        return new ResultadoProcessamentoFila(null, 0, false);
    }

    // Verifica se a lista contém um caminhão grande específico
    private boolean contemCaminhaoGrande(CaminhaoGrande caminhao) {
        for (int i = 0; i < listaGrandes.getTamanho(); i++) {
            if (listaGrandes.obter(i) == caminhao) {
                return true;
            }
        }
        return false;
    }

    // Encontra o índice de um caminhão grande na lista
    private int indiceDeCaminhaoGrande(CaminhaoGrande caminhao) {
        for (int i = 0; i < listaGrandes.getTamanho(); i++) {
            if (listaGrandes.obter(i) == caminhao) {
                return i;
            }
        }
        return -1;
    }

    // Atribui um caminhão grande à estação
    public void atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        if (!contemCaminhaoGrande(caminhao)) { // Verifica se o caminhão já está na lista
            caminhaoGrandeEsperando = caminhao; // Último caminhão atribuído é o ativo
            temCaminhaoGrandeEsperando = true;
            tempoEsperaCaminhaoGrande = 0;
            listaGrandes.adicionar(caminhao);
            LoggerSimulacao.log("ATRIBUICAO", String.format("%s: Caminhão grande %s atribuído", nome, caminhao.getPlaca()));
            Simulador.getEstatisticas().registrarNovoCaminhaoGrande();
        }
    }

    // Verifica se o tempo máximo de espera para caminhões pequenos foi excedido
    public boolean tempoEsperaExcedido() {
        if (!filaPequenos.estaVazia()) {
            for (int i = 0; i < filaPequenos.getTamanho(); i++) {
                CaminhaoPequeno caminhao = filaPequenos.obter(i);
                if (caminhao.getTempoEsperaFila() > esperaMaxPequenos) {
                    LoggerSimulacao.log("INFO", String.format("%s: Tempo de espera excedido para caminhão %s (%d min > %d min). Necessário novo caminhão grande.",
                            nome, caminhao.getPlaca(), caminhao.getTempoEsperaFila(), esperaMaxPequenos));
                    return true;
                }
            }
        }
        return false;
    }

    // Atualiza o tempo de espera do caminhão grande
    public void atualizarTempoEsperaCaminhaoGrande() {
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null) {
            tempoEsperaCaminhaoGrande++;
        }
    }

    // Libera o caminhão grande se necessário
    public CaminhaoGrande liberarCaminhaoGrandeSeNecessario() {
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando.getCargaAtual() > 0) {
            boolean toleranciaExcedida = tempoEsperaCaminhaoGrande >= caminhaoGrandeEsperando.getToleranciaEspera();
            boolean caminhaoCheio = caminhaoGrandeEsperando.getCargaAtual() == caminhaoGrandeEsperando.getCapacidade();
            if (toleranciaExcedida || caminhaoCheio) {
                CaminhaoGrande liberado = caminhaoGrandeEsperando;
                int carga = liberado.getCargaAtual();
                liberado.setEstacaoOrigem(this);
                liberado.setEstacaoDestino(this);
                int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(zonaDaEstacao, Simulador.getZonaAterro());
                liberado.iniciarViagemParaAterro(tempoViagem);
                // Remove o caminhão liberado da lista
                int indice = indiceDeCaminhaoGrande(liberado);
                if (indice != -1) {
                    listaGrandes.remover(indice);
                }
                // Escolhe o próximo caminhão grande da lista, se houver
                if (!listaGrandes.estaVazia()) {
                    caminhaoGrandeEsperando = listaGrandes.obter(0);
                    tempoEsperaCaminhaoGrande = 0;
                } else {
                    caminhaoGrandeEsperando = null;
                    temCaminhaoGrandeEsperando = false;
                    tempoEsperaCaminhaoGrande = 0;
                }
                LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão grande %s liberado para o aterro com %dkg (motivo: %s)",
                        nome, liberado.getPlaca(), carga,
                        caminhaoCheio ? "caminhão cheio" : "tolerância excedida"));
                return liberado;
            }
        }
        return null;
    }

    // Getters
    public String getNome() { return nome; }
    public Fila<CaminhaoPequeno> getFilaPequenos() { return filaPequenos; }
    public ZonaUrbana getZonaDaEstacao() { return zonaDaEstacao; }
    public boolean temCaminhaoGrandeFila() { return temCaminhaoGrandeEsperando; }
}



