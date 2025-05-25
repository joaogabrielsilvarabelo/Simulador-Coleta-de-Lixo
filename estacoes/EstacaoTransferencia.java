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
    private final Fila<CaminhaoPequeno> filaPequenos;
    private final Lista<CaminhaoGrande> listaGrandes; // Lista de caminhões grandes na estação
    private final int esperaMaxPequenos;
    private int esperaTotalPequenos; // Acumula tempo de espera para verificar excedente
    private final Lista<CaminhaoGrande> caminhoesGrandesEsperando; // Lista de caminhões grandes esperando
    private final Lista<Descarregamento> descarregamentosAtivos; // Lista de descarregamentos em andamento
    private ZonaUrbana zonaDaEstacao;
    private static final int TEMPO_DESCARREGAMENTO_POR_KG = 1; // 1 minuto por 1000kg

    public EstacaoTransferencia(String nome, int esperaMaxPequenos, ZonaUrbana zonaDaEstacao) {
        this.nome = nome;
        this.filaPequenos = new Fila<>();
        this.listaGrandes = new Lista<>();
        this.caminhoesGrandesEsperando = new Lista<>();
        this.descarregamentosAtivos = new Lista<>();
        this.esperaMaxPequenos = esperaMaxPequenos;
        this.esperaTotalPequenos = 0;
        this.zonaDaEstacao = zonaDaEstacao;
    }

    // Recebe um caminhão pequeno e o adiciona à fila
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao, int tempoAtual) {
        filaPequenos.enfileirar(caminhao);
        caminhao.incrementarTempoEspera();
        LoggerSimulacao.log("CHEGADA", String.format("%s: Caminhão %s chegou à estação e entrou na fila. Tamanho da fila: %d",
                nome, caminhao.getPlaca(), filaPequenos.getTamanho()));
    }

    // Processa a fila de caminhões pequenos, descarregando em múltiplos caminhões grandes
    public ResultadoProcessamentoFila processarFila(int tempoAtual) {
        // Atualiza tempo de espera dos caminhões pequenos
        int esperaAcumulada = atualizarEsperaCaminhoesPequenos();
        esperaTotalPequenos += esperaAcumulada;

        // Processa descarregamentos em andamento
        processarDescarregamentosAtivos();

        // Verifica se há caminhões pequenos na fila e caminhões grandes disponíveis
        if (!filaPequenos.estaVazia() && !caminhoesGrandesEsperando.estaVazia()) {
            return descarregarCaminhaoPequeno(tempoAtual);
        }
        return new ResultadoProcessamentoFila(null, 0, false);
    }

    // Atualiza o tempo de espera dos caminhões pequenos e retorna o acumulado
    private int atualizarEsperaCaminhoesPequenos() {
        int esperaAcumulada = 0;
        for (int i = 0; i < filaPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = filaPequenos.obter(i);
            caminhao.incrementarTempoEspera();
            esperaAcumulada += caminhao.getTempoEsperaFila();
        }
        return esperaAcumulada;
    }

    // Processa os descarregamentos em andamento, removendo os concluídos
    private void processarDescarregamentosAtivos() {
        for (int i = descarregamentosAtivos.getTamanho() - 1; i >= 0; i--) {
            Descarregamento descarregamento = descarregamentosAtivos.obter(i);
            if (descarregamento.atualizar()) {
                LoggerSimulacao.log("COLETA", String.format("%s: Caminhão grande %s recebeu a carga do caminhão %s",
                        nome, descarregamento.getCaminhaoGrande().getPlaca(), descarregamento.getCaminhaoPequeno().getPlaca()));
                descarregamentosAtivos.remover(i);
            }
        }
    }

    // Descarrega um caminhão pequeno em caminhões grandes disponíveis
    private ResultadoProcessamentoFila descarregarCaminhaoPequeno(int tempoAtual) {
        CaminhaoPequeno caminhaoPequeno = filaPequenos.remover();
        int cargaRestante = caminhaoPequeno.getCargaAtual();
        int cargaDescarregada = caminhaoPequeno.getCargaAtual();
        int tempoEsperaCaminhao = caminhaoPequeno.getTempoEsperaFila();
        boolean descarregouCompletamente = true;
        int tempoAtualDescarregamento = 0;
        int tempoDescarregamento = (int) Math.ceil(cargaDescarregada / 1000.0 * TEMPO_DESCARREGAMENTO_POR_KG);

        // Tenta descarregar no próximo caminhão grande disponível
        CaminhaoGrande caminhaoGrande = caminhoesGrandesEsperando.obter(0);
        while (cargaRestante > 0 && caminhaoGrande != null) {
           tempoAtualDescarregamento = descarregarEmCaminhaoGrande(caminhaoPequeno, caminhaoGrande, cargaRestante);
            cargaRestante = caminhaoPequeno.getCargaAtual();
            if (tempoAtualDescarregamento == 0){
                LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s descarregou %dkg em caminhão grande %s (tempo: %dmin)",
                        nome, caminhaoPequeno.getPlaca(), cargaDescarregada, caminhaoGrande.getPlaca(), tempoDescarregamento));
            }
            // Verifica se o caminhão grande está cheio
            if (caminhaoGrande.getCargaAtual() == caminhaoGrande.getCapacidade()) {
                liberarCaminhaoGrandeCheio(caminhaoGrande, tempoAtual);
                // Tenta próximo caminhão grande, se disponível
                caminhaoGrande = caminhoesGrandesEsperando.estaVazia() ? null : caminhoesGrandesEsperando.obter(0);
            } else {
                // Caminhão grande ainda tem capacidade, continua com ele
                break;
            }
        }

        // Se ainda há carga restante, o caminhão pequeno volta para a fila
        if (cargaRestante > 0) {
            filaPequenos.enfileirar(caminhaoPequeno);
            descarregouCompletamente = false;
            LoggerSimulacao.log("INFO", String.format("%s: Caminhão %s retornou à fila com %dkg restantes (sem caminhão grande disponível ou capacidade insuficiente)",
                    nome, caminhaoPequeno.getPlaca(), cargaRestante));
        } else {
            // Caminhão pequeno descarregou completamente
            caminhaoPequeno.resetarTempoEspera();
        }

        return new ResultadoProcessamentoFila(descarregouCompletamente ? caminhaoPequeno : null, tempoEsperaCaminhao, descarregouCompletamente);
    }

    // Descarrega carga do caminhão pequeno em um caminhão grande e retorna o tempo de descarregamento
    private int descarregarEmCaminhaoGrande(CaminhaoPequeno caminhaoPequeno, CaminhaoGrande caminhaoGrande, int cargaRestante) {
        int capacidadeLivre = caminhaoGrande.getCapacidade() - caminhaoGrande.getCargaAtual();
        if (capacidadeLivre > 0) {
            // Calcula quanto pode descarregar (mínimo entre carga restante e capacidade livre)
            int cargaDescarregada = Math.min(cargaRestante, capacidadeLivre);
            caminhaoGrande.carregar(cargaDescarregada);
            caminhaoPequeno.descarregarCarga(cargaDescarregada); // Descarrega parcialmente
            int tempoDescarregamento = (int) Math.ceil(cargaDescarregada / 1000.0 * TEMPO_DESCARREGAMENTO_POR_KG);

            // Log do descarregamento
            LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão %s iniciou a descarga de %dkg em caminhão grande %s (tempo: %dmin)",
                    nome, caminhaoPequeno.getPlaca(), cargaDescarregada, caminhaoGrande.getPlaca(), tempoDescarregamento));

            // Reseta tempo de espera do caminhão grande
            for (int i = 0; i < caminhoesGrandesEsperando.getTamanho(); i++) {
                if (caminhaoGrande == caminhoesGrandesEsperando.obter(i)) {
                    caminhoesGrandesEsperando.obter(i).resetarTempoEspera();
                    break;
                }
            }

            // Adiciona descarregamento à lista de ativos
            descarregamentosAtivos.adicionar(new Descarregamento(caminhaoPequeno, caminhaoGrande, tempoDescarregamento));

            return tempoDescarregamento;
        }
        // Caminhão grande sem capacidade, remove da espera
        caminhoesGrandesEsperando.remover(0);
        return 0;
    }

    // Libera um caminhão grande cheio para o aterro
    private void liberarCaminhaoGrandeCheio(CaminhaoGrande caminhaoGrande, int tempoAtual) {
        CaminhaoGrande liberado = caminhaoGrande;
        int carga = liberado.getCargaAtual();
        liberado.setEstacaoOrigem(this);
        liberado.setEstacaoDestino(this); // Retorna à mesma estação
        int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(zonaDaEstacao, Simulador.getZonaAterro());
        liberado.iniciarViagemParaAterro(tempoViagem);
        // Remove da lista de espera
        for (int j = 0; j < caminhoesGrandesEsperando.getTamanho(); j++) {
            if (caminhaoGrande == caminhoesGrandesEsperando.obter(j)) {
                caminhoesGrandesEsperando.remover(j);
                break;
            }
        }
        LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão grande %s liberado para o aterro com %dkg (caminhão cheio)",
                nome, liberado.getPlaca(), carga));
        // Notifica o simulador para registrar a liberação
        Simulador.adicionarCaminhaoGrandeOcupado(liberado);
    }

    // Atribui um caminhão grande à estação
    public void atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        caminhoesGrandesEsperando.adicionar(caminhao);
        listaGrandes.adicionar(caminhao);
        caminhao.setEstacaoOrigem(this); // Define a estação como origem permanente
    }

    // Verifica se o tempo máximo de espera para caminhões pequenos foi excedido
    public boolean tempoEsperaExcedido() {
        return esperaTotalPequenos > esperaMaxPequenos && !filaPequenos.estaVazia();
    }

    // Reseta o tempo de espera acumulado dos caminhões pequenos
    public void resetarEsperaTotalPequenos() {
        esperaTotalPequenos = 0;
        for (int i = 0; i < filaPequenos.getTamanho(); i++) {
            filaPequenos.obter(i).resetarTempoEspera();
        }
        LoggerSimulacao.log("INFO", String.format("%s: Tempo de espera dos caminhões pequenos resetado.", nome));
    }

    // Atualiza o tempo de espera dos caminhões grandes
    public void atualizarTempoEsperaCaminhaoGrande() {
        for (int i = 0; i < caminhoesGrandesEsperando.getTamanho(); i++) {
            CaminhaoGrande caminhao = caminhoesGrandesEsperando.obter(i);
            // Incrementa tempo de espera apenas se não está em descarregamento ativo
            boolean emDescarregamento = false;
            for (int j = 0; j < descarregamentosAtivos.getTamanho(); j++) {
                if (descarregamentosAtivos.obter(j).getCaminhaoGrande() == caminhao) {
                    emDescarregamento = true;
                    break;
                }
            }
            if (!emDescarregamento) {
                caminhao.incrementarTempoEspera();
            }
        }
    }

    // Libera caminhões grandes se necessário (cheios ou tolerância excedida)
    public CaminhaoGrande liberarCaminhaoGrandeSeNecessario() {
        for (int i = caminhoesGrandesEsperando.getTamanho() - 1; i >= 0; i--) {
            CaminhaoGrande caminhao = caminhoesGrandesEsperando.obter(i);
            // Verifica se o caminhão está em descarregamento ativo
            boolean emDescarregamento = false;
            for (int j = 0; j < descarregamentosAtivos.getTamanho(); j++) {
                if (descarregamentosAtivos.obter(j).getCaminhaoGrande() == caminhao) {
                    emDescarregamento = true;
                    break;
                }
            }
            if (!emDescarregamento) {
                boolean toleranciaExcedida = caminhao.getTempoEspera() >= caminhao.getToleranciaEspera();
                boolean caminhaoCheio = caminhao.getCargaAtual() == caminhao.getCapacidade();
                if (caminhao.getCargaAtual() > 0 && (toleranciaExcedida || caminhaoCheio)) {
                    CaminhaoGrande liberado = caminhao;
                    int carga = liberado.getCargaAtual();
                    liberado.setEstacaoOrigem(this);
                    liberado.setEstacaoDestino(this); // Retorna à mesma estação
                    int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(zonaDaEstacao, Simulador.getZonaAterro());
                    liberado.iniciarViagemParaAterro(tempoViagem);
                    caminhoesGrandesEsperando.remover(i);
                    LoggerSimulacao.log("DESCARGA", String.format("%s: Caminhão grande %s liberado para o aterro com %dkg (motivo: %s)",
                            nome, liberado.getPlaca(), carga,
                            caminhaoCheio ? "caminhão cheio" : "tolerância excedida"));
                    return liberado;
                }
            }
        }
        return null;
    }

    // Getters
    public String getNome() { return nome; }
    public Fila<CaminhaoPequeno> getFilaPequenos() { return filaPequenos; }
    public ZonaUrbana getZonaDaEstacao() { return zonaDaEstacao; }
    public boolean temCaminhaoGrandeFila() { return !caminhoesGrandesEsperando.estaVazia(); }

    // Representa uma operação de descarregamento em andamento
    class Descarregamento {
        private final CaminhaoPequeno caminhaoPequeno;
        private final CaminhaoGrande caminhaoGrande;
        private int tempoRestante;

        public Descarregamento(CaminhaoPequeno caminhaoPequeno, CaminhaoGrande caminhaoGrande, int tempoDescarregamento) {
            this.caminhaoPequeno = caminhaoPequeno;
            this.caminhaoGrande = caminhaoGrande;
            this.tempoRestante = tempoDescarregamento;
        }

        // Getters
        public CaminhaoPequeno getCaminhaoPequeno() { return caminhaoPequeno; }
        public CaminhaoGrande getCaminhaoGrande() { return caminhaoGrande; }
        public int getTempoRestante() { return tempoRestante; }

        // Atualiza o tempo restante e retorna true se o descarregamento terminou
        public boolean atualizar() {
            if (tempoRestante > 0) {
                tempoRestante--;
                return tempoRestante == 0;
            }
            return true;
        }
    }
}



