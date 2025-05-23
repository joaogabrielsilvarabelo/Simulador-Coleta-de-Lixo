package estacoes;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import caminhoes.DistribuicaoCaminhoes;
import estruturas.Fila;
import estruturas.Lista;
import simulacao.Estatisticas;
import simulacao.LoggerSimulacao;
import simulacao.Simulador;
import zonas.ZonaUrbana;

public class EstacaoTransferencia {
    private final String nome;
    private final Fila<CaminhaoPequeno> filaPequenos; // Fila de caminhões pequenos esperando para descarregar
    private final Lista<CaminhaoGrande> listaGrandes; // Lista de caminhões grandes associados à estação
    private final int esperaMaxPequenos; // Tempo máximo de espera para caminhões pequenos (min)
    private int esperaTotalPequenos; // Soma do tempo de espera de todos os caminhões pequenos
    public CaminhaoGrande caminhaoGrandeEsperando; // Caminhão grande aguardando para receber carga
    private ZonaUrbana zonaDaEstacao; // Zona onde a estação está localizada
    private boolean temCaminhaoGrandeEsperando; // Indica se há um caminhão grande esperando
    private int tempoEsperaCaminhaoGrande; // Tempo de espera do caminhão grande (min)
    private static final int TEMPO_DESCARREGAMENTO_POR_KG = 1; // 1 min por 1000kg descarregado

    // Construtor da estação de transferência
    public EstacaoTransferencia(String nome, int esperaMaxPequenos, ZonaUrbana zonaDaEstacao) {
        this.nome = nome;
        this.filaPequenos = new Fila<>();
        this.listaGrandes = new Lista<>();
        this.esperaMaxPequenos = esperaMaxPequenos;
        this.esperaTotalPequenos = 0;
        this.caminhaoGrandeEsperando = null;
        this.zonaDaEstacao = zonaDaEstacao;
        this.temCaminhaoGrandeEsperando = false;
        this.tempoEsperaCaminhaoGrande = 0;
        LoggerSimulacao.log("CONFIG", String.format("%s inicializada na zona %s com espera máxima de %dmin para pequenos.",
                nome, zonaDaEstacao.getNome(), esperaMaxPequenos));
    }

    // Recebe um caminhão pequeno e o adiciona à fila
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao, int tempoAtual) {
        filaPequenos.enfileirar(caminhao);
        caminhao.incrementarTempoEspera(); // Inicia contagem de espera
        LoggerSimulacao.log("CHEGADA", String.format("%s: Caminhão %s entrou na fila. Tamanho da fila: %d",
                nome, caminhao.getPlaca(), filaPequenos.getTamanho()));
    }

    // Escolhe outra estação para redirecionar caminhões pequenos se necessário
    private EstacaoTransferencia escolherOutraEstacao() {
        for (int i = 0; i < Simulador.getEstacoes().getTamanho(); i++) {
            EstacaoTransferencia estacao = Simulador.getEstacoes().obter(i);
            if (!estacao.getNome().equals(this.nome)) {
                return estacao; // Retorna a primeira estação diferente
            }
        }
        LoggerSimulacao.log("ERRO", String.format("%s: Nenhuma outra estação disponível para redirecionamento.", nome));
        return null;
    }

    // Processa a fila de caminhões pequenos, descarregando diretamente em caminhões grandes
    public ResultadoProcessamentoFila processarFila(int tempoAtual) {
        int esperaAcumulada = 0;
        boolean tempoExcedido = false;

        // Atualiza tempo de espera e verifica excedentes
        for (int i = 0; i < filaPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = filaPequenos.obter(i);
            caminhao.incrementarTempoEspera();
            esperaAcumulada += caminhao.getTempoEsperaFila();
            // Verifica se o tempo máximo de espera foi excedido
            if (caminhao.getTempoEsperaFila() > esperaMaxPequenos) {
                LoggerSimulacao.log("ERRO", String.format("%s: Caminhão %s excedeu tempo máximo de espera (%d min)",
                        nome, caminhao.getPlaca(), esperaMaxPequenos));
                tempoExcedido = true;
            }
        }
        esperaTotalPequenos += esperaAcumulada;

        // Solicita novo caminhão grande se não há um disponível e o tempo foi excedido
        if (tempoExcedido && !temCaminhaoGrandeEsperando) {
            solicitarNovoCaminhaoGrande();
        }

        // Se a fila está vazia, não há processamento
        if (filaPequenos.estaVazia()) {
            return new ResultadoProcessamentoFila(null, 0, false);
        }

        // Processa caminhões pequenos que estão descarregando
        for (int i = 0; i < filaPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = filaPequenos.obter(i);
            int cargaDescarregada = caminhao.getCargaAtual();
            if (caminhao.getEstado() == 5 && caminhao.getTempoDescarregamentoRestante() > 0) {
                // Continua descarregamento incremental
                if (caminhao.processarDescarregamento(caminhaoGrandeEsperando)) {
                    // Descarregamento concluído
                    CaminhaoPequeno caminhaoProcessado = filaPequenos.remover();
                    int tempoEspera = caminhaoProcessado.getTempoEsperaFila();
                    caminhaoProcessado.resetarTempoEspera();
                    LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s finalizou descarregamento de %dkg em caminhão grande %s.",
                            nome, caminhaoProcessado.getPlaca(), cargaDescarregada,
                            caminhaoGrandeEsperando != null ? caminhaoGrandeEsperando.getPlaca() : "N/A"));
                    return new ResultadoProcessamentoFila(caminhaoProcessado, tempoEspera, true);
                }
            }
        }

        // Inicia novo descarregamento se houver caminhão grande disponível
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null && !filaPequenos.estaVazia()) {
            CaminhaoPequeno caminhao = filaPequenos.primeiroDaFila();
            // Verifica se o caminhão está pronto para descarregar (não está descarregando)
            if (caminhao.getEstado() != 5 && caminhao.getCargaAtual() > 0) {
                int cargaDescarregada = caminhao.getCargaAtual();
                // Verifica se o caminhão grande tem capacidade suficiente
                if (cargaDescarregada <= caminhaoGrandeEsperando.getCapacidade() - caminhaoGrandeEsperando.getCargaAtual()) {
                    // Inicia descarregamento incremental
                    caminhao.iniciarDescarregamento(cargaDescarregada, TEMPO_DESCARREGAMENTO_POR_KG);
                    caminhao.setEstado(5); // DESCARREGANDO
                    LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s iniciou descarregamento de %dkg em caminhão grande %s (tempo: %dmin)",
                            nome, caminhao.getPlaca(), cargaDescarregada, caminhaoGrandeEsperando.getPlaca(),
                            caminhao.getTempoDescarregamentoRestante()));
                    return new ResultadoProcessamentoFila(null, 0, false);
                } else {
                    // Caminhão grande não tem capacidade suficiente; solicita novo caminhão
                    if (tempoExcedido) {
                        solicitarNovoCaminhaoGrande();
                    }
                }
            }
        }

        // Se o tempo foi excedido e há caminhões na fila, tenta redirecionar
        if (tempoExcedido && !temCaminhaoGrandeEsperando && !filaPequenos.estaVazia()) {
            EstacaoTransferencia outraEstacao = escolherOutraEstacao();
            if (outraEstacao != null) {
                CaminhaoPequeno caminhao = filaPequenos.remover();
                int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(caminhao.getZonaAtual(), outraEstacao.getZonaDaEstacao());
                caminhao.definirTempoViagem(tempoViagem);
                caminhao.setEstacaoDestino(outraEstacao);
                LoggerSimulacao.log("INFO", String.format("Caminhão %s redirecionado para %s (viagem: %dmin)",
                        caminhao.getPlaca(), outraEstacao.getNome(), tempoViagem));
                return new ResultadoProcessamentoFila(caminhao, caminhao.getTempoEsperaFila(), true);
            }
        }

        return new ResultadoProcessamentoFila(null, 0, false);
    }

    // Solicita um novo caminhão grande ao Simulador
    private void solicitarNovoCaminhaoGrande() {
        // Cria um novo caminhão grande com a tolerância configurada
        CaminhaoGrande novoCaminhao = new CaminhaoGrande(Simulador.getToleranciaCaminhoesGrandes());
        // Adiciona à lista de todos os caminhões grandes
        Simulador.getCaminhoesGrandes().adicionar(novoCaminhao);
        // Atribui à estação
        atribuirCaminhaoGrande(novoCaminhao);
        LoggerSimulacao.log("INFO", String.format("%s: Novo caminhão grande %s solicitado devido a tempo de espera excedido.", nome, novoCaminhao.getPlaca()));
    }

    // Atribui um caminhão grande à estação
    public void atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        if (!temCaminhaoGrandeEsperando) {
            caminhaoGrandeEsperando = caminhao;
            temCaminhaoGrandeEsperando = true;
            tempoEsperaCaminhaoGrande = 0;
            listaGrandes.adicionar(caminhao);
            LoggerSimulacao.log("ATRIBUICAO", String.format("%s: Caminhão grande %s atribuído", nome, caminhao.getPlaca()));
            Estatisticas estatisticas = new Estatisticas();
            estatisticas.registrarNovoCaminhaoGrande();
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
        }
    }

    // Libera o caminhão grande se necessário (cheio ou tolerância excedida)
    public CaminhaoGrande liberarCaminhaoGrandeSeNecessario() {
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null &&
                caminhaoGrandeEsperando.getCargaAtual() > 0) {
            // Verifica condições para liberação
            boolean toleranciaExcedida = tempoEsperaCaminhaoGrande >= caminhaoGrandeEsperando.getToleranciaEspera();
            boolean caminhaoCheio = caminhaoGrandeEsperando.getCargaAtual() == caminhaoGrandeEsperando.getCapacidade();
            if (toleranciaExcedida || caminhaoCheio) {
                CaminhaoGrande liberado = caminhaoGrandeEsperando;
                int carga = liberado.getCargaAtual();
                // Define estação de origem
                liberado.setEstacaoOrigem(this);
                // Define estação de destino (retorna à mesma estação por padrão)
                liberado.setEstacaoDestino(this);
                // Calcula tempo de viagem para o aterro
                int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(zonaDaEstacao, Simulador.getZonaAterro());
                liberado.iniciarViagemParaAterro(tempoViagem);
                caminhaoGrandeEsperando = null;
                temCaminhaoGrandeEsperando = false;
                tempoEsperaCaminhaoGrande = 0;
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


