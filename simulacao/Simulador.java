package simulacao;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.DistribuicaoCaminhoes;
import estacoes.EstacaoTransferencia;
import estacoes.ResultadoProcessamentoFila;
import estruturas.Lista;
import zonas.ZonaUrbana;
import zonas.ZonaEstatistica;

public class Simulador {
    private static int[][] intervalosLixo;
    private final DistribuicaoCaminhoes distribuicaoCaminhoes;
    private static Lista<CaminhaoPequeno> caminhoesPequenos;
    private static Lista<ZonaUrbana> zonas;
    private static Lista<EstacaoTransferencia> estacoes;
    private static Lista<CaminhaoGrande> caminhoesGrandesDisponiveis;
    private static Lista<CaminhaoGrande> caminhoesGrandesOcupados = new Lista<>();
    private static Lista<CaminhaoGrande> todosCaminhoesGrandes = new Lista<>();
    private static ZonaUrbana zonaAterro;
    private static final Estatisticas estatisticas = new Estatisticas();
    private static int caminhoesPorZona;
    private static int tempoSimulado;
    private static int toleranciaCaminhoesGrandes;
    private static final int TEMPO_MINUTOS_POR_DIA = 24 * 60;
    private boolean rodando;
    private boolean pausado;

    // Construtor da simulação
    public Simulador() {
        caminhoesPequenos = new Lista<>();
        zonas = new Lista<>();
        estacoes = new Lista<>();
        caminhoesGrandesDisponiveis = new Lista<>();
        this.distribuicaoCaminhoes = new DistribuicaoCaminhoes();
        caminhoesPorZona = 0;
        tempoSimulado = 0;
        toleranciaCaminhoesGrandes = 0;
        this.rodando = false;
        this.pausado = false;
    }

    // Inicia a simulação em uma nova thread
    public void iniciar() {
        if (!rodando) {
            rodando = true;
            pausado = false;
            new Thread(this::executarSimulacao).start();
        } else {
            LoggerSimulacao.log("ERRO", "Simulação já está rodando!");
        }
    }

    // Pausa a simulação
    public void pausar() {
        if (rodando && !pausado) {
            pausado = true;
            LoggerSimulacao.log("INFO", "Simulação pausada.");
        } else {
            LoggerSimulacao.log("ERRO", "Simulação não está rodando ou já está pausada!");
        }
    }

    // Continua a simulação pausada
    public void continuarSimulacao() {
        if (rodando && pausado) {
            pausado = false;
            LoggerSimulacao.log("INFO", "Simulação continuada.");
        } else {
            LoggerSimulacao.log("ERRO", "Simulação não está pausada ou não foi iniciada!");
        }
    }

    // Encerra a simulação e imprime relatório final
    public void encerrar() {
        if (rodando) {
            rodando = false;
            pausado = false;
            LoggerSimulacao.log("INFO", "Simulação encerrada.");
            estatisticas.imprimirRelatorio();
        }
    }

    // Configura parâmetros da simulação
    protected static void configurarSimuladorParams(int tolerancia) {
        setListaCaminhoesPequenos(caminhoesPequenos);
        setListaZonas(zonas);
        setListaEstacoes(estacoes);
        setToleranciaCaminhoesGrandes(tolerancia);
        inicializarCaminhoesGrandes(estacoes.getTamanho());
    }

    // Executa o loop principal da simulação
    private void executarSimulacao() {
        while (rodando) {
            if (!pausado) {
                atualizarSimulacao();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LoggerSimulacao.log("ERRO", "Erro na simulação: " + e.getMessage());
            }
        }
    }

    // Atualiza o estado da simulação, gerenciando coletas, transferências e avanço do dia
    private void atualizarSimulacao() {
        tempoSimulado++;
        // Verifica se o dia terminou (o lixo total foi coletado e enviado ao aterro)
        if (isDiaConcluido()) {
            LoggerSimulacao.log("INFO", "Todo o lixo do dia foi coletado e enviado ao aterro. Avançando para o próximo dia.");
            concluirDia();
            tempoSimulado = ((tempoSimulado / TEMPO_MINUTOS_POR_DIA) + 1) * TEMPO_MINUTOS_POR_DIA;
            estatisticas.setTempoSimulado(tempoSimulado);
            estatisticas.imprimirRelatorio();
            return;
        }
        // Reinicia no fim do dia
        if (tempoSimulado % TEMPO_MINUTOS_POR_DIA == 0 && tempoSimulado != 1) {
            concluirDia();
        }
        // Redistribui caminhões ociosos apenas se houver trabalho pendente
        if (temLixoOuTrabalhoPendente() && tempoSimulado % 10 == 0) {
            distribuirCaminhoesDisponiveis();
        }
        processarCaminhoesPequenos();
        processarEstacoes();
        processarCaminhoesGrandesOcupados();
        // Imprime relatório a cada hora
        if (tempoSimulado % 60 == 0) {
            estatisticas.setTempoSimulado(tempoSimulado);
            estatisticas.imprimirRelatorio();
        }
    }

    // Conclui o dia, processando lixo residual e reiniciando estados
    private void concluirDia() {
        processarLixoResidualCaminhoesPequenos();
        limparEstacoes();
        enviarCaminhoesGrandesParaAterro();
        gerarLixoZonas();
        reiniciarCaminhoesPequenos();
    }

    // Verifica se há lixo ou trabalho pendente para evitar redistribuição desnecessária
    private boolean temLixoOuTrabalhoPendente() {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            if (zonas.obter(i).getLixoAcumulado() > 0) {
                return true;
            }
        }
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getCargaAtual() > 0 || caminhao.getEstado() == 3) {
                return true;
            }
        }
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (!estacao.getFilaPequenos().estaVazia()) {
                return true;
            }
        }
        return false;
    }

    // Verifica se o dia está concluído (sem lixo, caminhões ativos, filas e o lixo total no aterro)
    private boolean isDiaConcluido() {
        // Verifica lixo nas zonas
        for (int i = 0; i < zonas.getTamanho(); i++) {
            if (zonas.obter(i).getLixoAcumulado() > 0) {
                return false;
            }
        }
        // Verifica caminhões pequenos com carga ou em estados ativos
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            int estado = caminhao.getEstado();
            if (caminhao.getCargaAtual() > 0 && estado != 4 && estado != 5) {
                return false;
            }
            if (estado == 2 || estado == 3) {
                return false;
            }
        }
        // Verifica filas nas estações
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (!estacao.getFilaPequenos().estaVazia()) {
                return false;
            }
        }
        // Verifica caminhões grandes com carga
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (estacao.temCaminhaoGrandeFila() && estacao.caminhaoGrandeEsperando != null && estacao.caminhaoGrandeEsperando.getCargaAtual() > 0) {
                return false;
            }
        }
        // Verifica se o lixo total coletado foi enviado ao aterro
        return estatisticas.getTotalLixoColetado() == estatisticas.getTotalLixoAterro();
    }

    // Reativa caminhões pequenos que atingiram o limite de viagens
    private void reiniciarCaminhoesPequenos() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() == 6) { // ENCERRADO
                caminhao.viagensFeitas = 0;
                caminhao.setEstado(1); // DISPONÍVEL
                LoggerSimulacao.log("INFO", String.format("Caminhão %s reativado para novo dia.", caminhao.getPlaca()));
            }
        }
    }

    // Descarrega lixo residual de caminhões pequenos ao fim do dia
    private void processarLixoResidualCaminhoesPequenos() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getCargaAtual() > 0 && caminhao.getEstado() != 4 && caminhao.getEstado() != 5) {
                EstacaoTransferencia estacao = escolherEstacao(caminhao);
                if (estacao != null) {
                    int carga = caminhao.descarregar();
                    estacao.receberCaminhaoPequeno(caminhao, tempoSimulado);
                    LoggerSimulacao.log("DESCARGA", String.format("Caminhão %s descarregou %dkg residual em %s.",
                            caminhao.getPlaca(), carga, estacao.getNome()));
                }
            }
        }
    }

    // Processa filas nas estações, garantindo que todo lixo residual seja transferido
    private void limparEstacoes() {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            // Processa filas de caminhões pequenos
            while (!estacao.getFilaPequenos().estaVazia()) {
                ResultadoProcessamentoFila resultado = estacao.processarFila(tempoSimulado);
                if (resultado.foiProcessado()) {
                    estatisticas.registrarEspera(resultado.getTempoDeEspera());
                    CaminhaoPequeno pequeno = resultado.getCaminhaoProcessado();
                    if (pequeno != null) {
                        pequeno.setEstado(1); // DISPONÍVEL
                        LoggerSimulacao.log("DESCARGA", String.format("Caminhão %s processado em %s (limpeza residual).",
                                pequeno.getPlaca(), estacao.getNome()));
                    }
                }
            }
        }
    }

    // Envia caminhões grandes com qualquer carga ao aterro no fim do dia
    private void enviarCaminhoesGrandesParaAterro() {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (estacao.temCaminhaoGrandeFila() && estacao.caminhaoGrandeEsperando != null && estacao.caminhaoGrandeEsperando.getCargaAtual() > 0) {
                CaminhaoGrande grande = estacao.liberarCaminhaoGrandeSeNecessario();
                if (grande != null) {
                    caminhoesGrandesOcupados.adicionar(grande);
                }
            }
        }
    }

    // Gera lixo nas zonas no início do dia
    protected static void gerarLixoZonas() {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            int gerado = zona.gerarLixo();
            estatisticas.registrarGeracaoLixo(zona.getNome(), gerado);
        }
    }

    // Distribui caminhões disponíveis para zonas com lixo
    private void distribuirCaminhoesDisponiveis() {
        if (!zonas.estaVazia()) {
            int distribuidos = distribuicaoCaminhoes.distribuirCaminhoes(caminhoesPequenos, zonas);
            if (distribuidos > 0) {
                LoggerSimulacao.log("INFO", distribuidos + " caminhão(ões) distribuído(s).");
            }
            // No modo DEBUG, loga mesmo se nenhum caminhão for distribuído
            if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog() && distribuidos == 0) {
                LoggerSimulacao.log("INFO", "Nenhum caminhão distribuído.");
            }
        }
    }

    // Inicializa as zonas com intervalos de lixo configuráveis
    public static void inicializarZonas(int[][] intervalos) {
        intervalosLixo = intervalos;
        zonas.limpar();
        for (int i = 0; i < 5; i++) {
            ZonaUrbana zona = new ZonaUrbana(i, intervalosLixo[i][0], intervalosLixo[i][1]);
            zonas.adicionar(zona);
            LoggerSimulacao.log("CONFIG", String.format("Zona %s inicializada com intervalo de lixo [%d, %d]kg.",
                    zona.getNome(), intervalosLixo[i][0], intervalosLixo[i][1]));
        }
    }

    // Inicializa caminhões pequenos com capacidades e zonas iniciais
    protected static void inicializarCaminhoes(int qtd2t, int qtd4t, int qtd8t, int qtd10t, int limiteViagens) {
        int totalCaminhoes = qtd2t + qtd4t + qtd8t + qtd10t;
        int TOTAL_ZONAS = 5;
        caminhoesPorZona = totalCaminhoes / TOTAL_ZONAS;
        int extras = totalCaminhoes % TOTAL_ZONAS;

        int[] quantidades = {qtd2t, qtd4t, qtd8t, qtd10t};
        int caminhaoIdx = 0;

        for (int i = 0; i < TOTAL_ZONAS; i++) {
            ZonaUrbana zonaInicial = zonas.obter(i);
            int caminhoesParaZona = caminhoesPorZona + (i < extras ? 1 : 0);

            for (int j = 0; j < caminhoesParaZona && caminhaoIdx < totalCaminhoes; j++) {
                // Escolher tipo de caminhão
                for (int k = 0; k < quantidades.length; k++) {
                    if (quantidades[k] > 0) {
                        CaminhaoPequeno caminhao = new CaminhaoPequeno(k + 1, limiteViagens, zonaInicial);
                        caminhao.setEstado(2); // COLETANDO
                        caminhoesPequenos.adicionar(caminhao);
                        zonaInicial.incrementarCaminhoesAtivos(); // Incrementa caminhões ativos na zona
                        quantidades[k]--;
                        caminhaoIdx++;
                        if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
                            LoggerSimulacao.log("INFO", String.format("Caminhão %s inicializado em %s, estado COLETANDO.",
                                    caminhao.getPlaca(), zonaInicial.getNome()));
                        }
                        break;
                    }
                }
            }
        }
    }

    // Inicializa estações de transferência
    protected static void inicializarEstacoes(int tempoMaxEspera, ZonaUrbana zonaEstacaoA, ZonaUrbana zonaEstacaoB) {
        estacoes.adicionar(new EstacaoTransferencia("Estação A", tempoMaxEspera, zonaEstacaoA));
        estacoes.adicionar(new EstacaoTransferencia("Estação B", tempoMaxEspera, zonaEstacaoB));
    }

    // Inicializa a zona do aterro
    public static void inicializarAterro(ZonaUrbana zona) {
        zonaAterro = zona;
    }

    // Inicializa estatísticas das zonas para processamento de coletas
    private void inicializarZonaEstatistica() {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            ZonaEstatistica estatistica = estatisticas.buscarZonaEstatistica(zona.getNome());
            if (estatistica == null) {
                estatistica = new ZonaEstatistica(zona.getNome());
                estatisticas.getLixoPorZona().adicionar(estatistica);
            }
            estatistica.inicializarCicloColeta(zona.getLixoAcumulado());
        }
    }

    // Inicializa caminhões grandes, atribuindo diretamente às estações
    protected static void inicializarCaminhoesGrandes(int quantidade) {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (i < quantidade) {
                CaminhaoGrande caminhao = new CaminhaoGrande(toleranciaCaminhoesGrandes);
                todosCaminhoesGrandes.adicionar(caminhao);
                estacao.atribuirCaminhaoGrande(caminhao);
            }
        }
    }

    // Adiciona um novo caminhão grande a uma estação específica
    private void adicionarCaminhaoGrande(EstacaoTransferencia estacao) {
        CaminhaoGrande novoCaminhao = new CaminhaoGrande(toleranciaCaminhoesGrandes);
        todosCaminhoesGrandes.adicionar(novoCaminhao);
        estacao.atribuirCaminhaoGrande(novoCaminhao);
        LoggerSimulacao.log("ATRIBUICAO", String.format("Novo caminhão grande %s adicionado à %s devido a tempo de espera excedido.",
                novoCaminhao.getPlaca(), estacao.getNome()));
    }

    // Processa ações dos caminhões pequenos (coleta, trânsito)
    private void processarCaminhoesPequenos() {
        inicializarZonaEstatistica();
        processarColetas();
        if (tempoSimulado % 60 == 0) {
            logTentativasSemColeta();
        }
        // Procura por caminhões em zonas com 0 lixo ou que terminaram a coleta
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            caminhao.isLimiteAtingido(caminhao.getViagensFeitas());
            if (caminhao.getEstado() == 6) { // ENCERRADO
                continue; // Pula caminhões encerrados
            }
            if (caminhao.getEstado() == 2 && caminhao.getZonaAtual() != null) {
                if (caminhao.getZonaAtual().getLixoAcumulado() == 0 && caminhao.getTempoColetaRestante() == 0) {
                    // Sem lixo na zona e sem coleta em andamento
                    caminhao.setEstado(1); // DISPONÍVEL
                    caminhao.getZonaAtual().decrementarCaminhoesAtivos();
                    LoggerSimulacao.log("INFO", String.format("Caminhão %s em %s (sem lixo ou coleta concluída) agora disponível para redistribuição.",
                            caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
                } else if (caminhao.getTempoColetaRestante() == 0 && !caminhao.estaCheio()) {
                    // Coleta concluída, mas caminhão não está cheio
                    int lixoColetado = caminhao.coletar(caminhao.getZonaAtual().getLixoAcumulado());
                    if (lixoColetado == 0 && caminhao.getCargaAtual() == 0) {
                        // Zona não tem mais lixo para coletar e o caminhão está sem carga
                        caminhao.setEstado(1); // DISPONÍVEL
                        caminhao.getZonaAtual().decrementarCaminhoesAtivos();
                        LoggerSimulacao.log("INFO", String.format("Caminhão %s em %s não coletou (sem lixo suficiente) e agora está disponível para redistribuição.",
                                caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
                    }
                }
            }
        }
        processarCaminhoesEmTransito();
    }

    // Processa coletas dos caminhões pequenos
    private void processarColetas() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() == 2 && caminhao.getTempoColetaRestante() > 0) {
                if (caminhao.processarColeta()) {
                    if (caminhao.estaCheio()) {
                        EstacaoTransferencia estacao = escolherEstacao(caminhao);
                        if (estacao != null) {
                            int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(caminhao.getZonaAtual(), estacao.getZonaDaEstacao());
                            caminhao.definirTempoViagem(tempoViagem);
                            caminhao.setEstacaoDestino(estacao);
                            caminhao.setEstado(3); // EM_TRÂNSITO
                            LoggerSimulacao.log("VIAGEM", String.format("Caminhão %s partiu de %s para %s (viagem: %dmin).",
                                    caminhao.getPlaca(), caminhao.getZonaAtual().getNome(), estacao.getNome(), tempoViagem));
                        }
                    }
                }
            }
        }
    }

    // Registra tentativas de coleta sem sucesso
    private void logTentativasSemColeta() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() == 2 && caminhao.getZonaAtual() != null && caminhao.getZonaAtual().getLixoAcumulado() == 0 && caminhao.getCargaAtual() == 0) {
                LoggerSimulacao.log("INFO", String.format("Caminhão %s em %s não coletou (sem lixo disponível).",
                        caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
            }
        }
    }

    // Processa caminhões pequenos em trânsito
    private void processarCaminhoesEmTransito() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() == 3 && caminhao.processarViagem()) {
                if (caminhao.getZonaDestino() != null) {
                    // Indo à zona
                    if (caminhao.getZonaAtual() != caminhao.getZonaDestino()) {
                        caminhao.getZonaAtual().decrementarCaminhoesAtivos(); // Diminui caminhões ativos na zona origem
                        caminhao.setZonaAtual(caminhao.getZonaDestino());
                        caminhao.viagensFeitas++;
                    }
                    caminhao.setZonaDestino(null);
                    caminhao.setEstado(2); // COLETANDO
                    caminhao.getZonaAtual().incrementarCaminhoesAtivos();
                    LoggerSimulacao.log("CHEGADA", String.format("Caminhão %s chegou à zona %s, iniciando coleta",
                            caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
                } else if (caminhao.getEstacaoDestino() != null) {
                    // Indo à estação
                    caminhao.setEstado(4); // FILA_ESTAÇÃO
                    caminhao.getEstacaoDestino().receberCaminhaoPequeno(caminhao, tempoSimulado);
                    caminhao.setEstacaoDestino(null);
                    caminhao.viagensFeitas++;
                }
            }
        }
    }

    // Processa estações, verificando filas e adicionando caminhões grandes se necessário
    private void processarEstacoes() {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            // Verifica se o tempo de espera foi excedido e adiciona novo caminhão grande
            if (estacao.tempoEsperaExcedido()) {
                adicionarCaminhaoGrande(estacao);
            }
            // Processa a fila de caminhões pequenos
            ResultadoProcessamentoFila resultado = estacao.processarFila(tempoSimulado);
            if (resultado.foiProcessado()) {
                estatisticas.registrarEspera(resultado.getTempoDeEspera());
                CaminhaoPequeno pequeno = resultado.getCaminhaoProcessado();
                if (pequeno != null) {
                    pequeno.setEstado(1); // DISPONÍVEL (redundante, já setado em EstacaoTransferencia)
                    pequeno.setEstacaoDestino(null);
                }
            }
            // Atualiza tempo de espera do caminhão grande
            estacao.atualizarTempoEsperaCaminhaoGrande();
            // Libera caminhão grande se necessário
            CaminhaoGrande grande = estacao.liberarCaminhaoGrandeSeNecessario();
            if (grande != null) {
                caminhoesGrandesOcupados.adicionar(grande);
            }
        }
    }

    // Processa caminhões grandes em trânsito
    private void processarCaminhoesGrandesOcupados() {
        for (int i = caminhoesGrandesOcupados.getTamanho() - 1; i >= 0; i--) {
            CaminhaoGrande caminhao = caminhoesGrandesOcupados.obter(i);
            // Atualiza estado do caminhão
            if (caminhao.atualizarEstado()) {
                // Caminhão retornou à estação, remove da lista
                caminhoesGrandesOcupados.remover(i);
                EstacaoTransferencia estacao = caminhao.getEstacaoDestino();
                if (estacao != null) {
                    estacao.atribuirCaminhaoGrande(caminhao);
                    LoggerSimulacao.log("CHEGADA", String.format("Caminhão grande %s retornou à %s.",
                            caminhao.getPlaca(), estacao.getNome()));
                } else {
                    caminhoesGrandesDisponiveis.adicionar(caminhao);
                    LoggerSimulacao.log("INFO", String.format("Caminhão grande %s disponível (sem estação atribuída).",
                            caminhao.getPlaca()));
                }
            }
        }
    }

    // Escolhe a estação mais próxima para um caminhão pequeno
    private EstacaoTransferencia escolherEstacao(CaminhaoPequeno caminhao) {
        EstacaoTransferencia maisProxima = null;
        int menorTempo = Integer.MAX_VALUE;
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(caminhao.getZonaAtual(), estacao.getZonaDaEstacao());
            if (tempoViagem < menorTempo) {
                menorTempo = tempoViagem;
                maisProxima = estacao;
            }
        }
        return maisProxima;
    }

    // Getters e Setters
    public static int getTempoSimulado() { return tempoSimulado; }
    public static int getCaminhoesPorZona() { return caminhoesPorZona; }
    protected static Lista<ZonaUrbana> getZonas() { return zonas; }
    public static ZonaUrbana getZonaAterro(){ return zonaAterro; }
    protected static Lista<CaminhaoPequeno> getCaminhoesPequenos() { return caminhoesPequenos; }
    public static Lista<CaminhaoGrande> getCaminhoesGrandes() { return todosCaminhoesGrandes; }
    public static void setListaZonas(Lista<ZonaUrbana> zonas) { Simulador.zonas = zonas; }
    public static void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> caminhoes) { caminhoesPequenos = caminhoes; }
    public static void setListaEstacoes(Lista<EstacaoTransferencia> estacoes) { Simulador.estacoes = estacoes; }
    public static void setToleranciaCaminhoesGrandes(int tolerancia) { toleranciaCaminhoesGrandes = tolerancia; }
    public static Estatisticas getEstatisticas() { return estatisticas; }
    public static Lista<EstacaoTransferencia> getEstacoes() { return estacoes; }
    public static int getToleranciaCaminhoesGrandes() { return toleranciaCaminhoesGrandes; }
}


