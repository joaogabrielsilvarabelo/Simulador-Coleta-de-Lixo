package simulacao;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.DistribuicaoCaminhoes;
import estacoes.EstacaoTransferencia;
import estacoes.ResultadoProcessamentoFila;
import estruturas.Lista;
import zonas.ZonaUrbana;

// Gerencia a simulação de coleta e transporte de lixo
public class Simulador {
    private static int[][] intervalosLixo;
    private final DistribuicaoCaminhoes distribuicaoCaminhoes;
    private static Lista<CaminhaoPequeno> caminhoesPequenos;
    private static Lista<ZonaUrbana> zonas;
    private static Lista<EstacaoTransferencia> estacoes;
    private static Lista<CaminhaoGrande> caminhoesGrandesDisponiveis;
    // Lista de caminhões grandes em trânsito (viagem, descarregamento ou retorno)
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

    public static void setListaZonas(Lista<ZonaUrbana> zonas) {
        Simulador.zonas = zonas;
    }

    public static void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> caminhoes) {
        caminhoesPequenos = caminhoes;
    }

    public static void setListaEstacoes(Lista<EstacaoTransferencia> estacoes) {
        Simulador.estacoes = estacoes;
    }

    public static void setToleranciaCaminhoesGrandes(int tolerancia) {
        toleranciaCaminhoesGrandes = tolerancia;
    }

    public static Estatisticas getEstatisticas() {
        return estatisticas;
    }

    public static Lista<EstacaoTransferencia> getEstacoes() {
        return estacoes;
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

    public void pausar() {
        if (rodando && !pausado) {
            pausado = true;
            LoggerSimulacao.log("INFO", "Simulação pausada.");
        } else {
            LoggerSimulacao.log("ERRO", "Simulação não está rodando ou já está pausada!");
        }
    }

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
            if (estacao.lixoArmazenado > 0 || !estacao.getFilaPequenos().estaVazia()) {
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
        // Verifica lixo ou filas nas estações
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (estacao.lixoArmazenado > 0 || !estacao.getFilaPequenos().estaVazia()) {
                return false;
            }
        }
        // Verifica caminhões grandes com carga
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (estacao.temCaminhaoGrandeFila() && estacao.caminhaoGrandeEsperando.getCargaAtual() > 0) {
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

    // Processa filas e transfere o lixo total armazenado nas estações
    private void limparEstacoes() {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            // Processa filas de caminhões pequenos
            while (!estacao.getFilaPequenos().estaVazia()) {
                ResultadoProcessamentoFila resultado = estacao.processarFila(tempoSimulado);
                if (resultado.foiProcessado()) {
                    estatisticas.registrarEspera(resultado.getTempoDeEspera());
                    CaminhaoPequeno pequeno = resultado.getCaminhaoProcessado();
                    pequeno.setEstado(1);
                    LoggerSimulacao.log("DESCARGA", String.format("Caminhão %s processado em %s (limpeza residual).",
                            pequeno.getPlaca(), estacao.getNome()));
                }
            }
            // Transfere o lixo total armazenado para caminhões grandes
            while (estacao.lixoArmazenado > 0) {
                // Atribui caminhão grande, se necessário
                if (!estacao.temCaminhaoGrandeFila()) {
                    if (!caminhoesGrandesDisponiveis.estaVazia()) {
                        CaminhaoGrande grande = caminhoesGrandesDisponiveis.remover(0);
                        estacao.atribuirCaminhaoGrande(grande);
                        LoggerSimulacao.log("ATRIBUICAO", String.format("Caminhão grande %s atribuído a %s para limpar lixo residual.", grande.getPlaca(), estacao.getNome()));
                    } else {
                        adicionarCaminhaoGrande(estacao);
                        LoggerSimulacao.log("ATRIBUICAO", String.format("Novo caminhão grande atribuído diretamente a %s para limpar lixo residual.", estacao.getNome()));
                    }
                }
                // Transfere o máximo possível de lixo
                int cargaTransferir = Math.min(estacao.lixoArmazenado, estacao.caminhaoGrandeEsperando.getCapacidade() - estacao.caminhaoGrandeEsperando.getCargaAtual());
                if (cargaTransferir > 0) {
                    estacao.caminhaoGrandeEsperando.carregar(cargaTransferir);
                    estacao.lixoArmazenado -= cargaTransferir;
                    LoggerSimulacao.log("DESCARGA", String.format("%s: Transferiu %dkg para caminhão grande %s. Lixo restante: %dkg.",
                            estacao.getNome(), cargaTransferir, estacao.caminhaoGrandeEsperando.getPlaca(), estacao.lixoArmazenado));
                }
                // Envia caminhão grande ao aterro se estiver cheio
                if (estacao.caminhaoGrandeEsperando.getCargaAtual() > 0) {
                    CaminhaoGrande grande = estacao.caminhaoGrandeEsperando;
                    estacao.caminhaoGrandeEsperando = null;
                    estacao.temCaminhaoGrandeEsperando = false;
                    estacao.tempoEsperaCaminhaoGrande = 0;
                    int carga = grande.getCargaAtual();
                    grande.descarregar();
                    caminhoesGrandesDisponiveis.adicionar(grande);
                    LoggerSimulacao.log("VIAGEM", String.format("Caminhão grande %s partiu de %s para o aterro con %dkg.", grande.getPlaca(), estacao.getNome(), carga));
                }
            }
        }
    }

    // Envia caminhões grandes com qualquer carga ao aterro no fim do dia
    private void enviarCaminhoesGrandesParaAterro() {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (estacao.temCaminhaoGrandeFila() && estacao.caminhaoGrandeEsperando.getCargaAtual() > 0) {
                CaminhaoGrande grande = estacao.caminhaoGrandeEsperando;
                estacao.caminhaoGrandeEsperando = null;
                estacao.temCaminhaoGrandeEsperando = false;
                estacao.tempoEsperaCaminhaoGrande = 0;
                int carga = grande.getCargaAtual();
                grande.descarregar();
                caminhoesGrandesDisponiveis.adicionar(grande);
                LoggerSimulacao.log("VIAGEM", String.format("Caminhão grande %s partiu de %s para o aterro com %dkg (fim do dia).",
                        grande.getPlaca(), estacao.getNome(), carga));
            }
        }
    }

    protected static void gerarLixoZonas() {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            int gerado = zona.gerarLixo();
            estatisticas.registrarGeracaoLixo(zona.getNome(), gerado);
        }
    }

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

    // Processa ações dos caminhões pequenos (coleta, trânsito)
    private void processarCaminhoesPequenos() {
        ZonaStats[] zonaStats = inicializarZonaStats();
        processarColetas(zonaStats);
        if (tempoSimulado % 60 == 0) {
            logTentativasSemColeta(zonaStats);
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

    // Processa caminhões grandes em trânsito
    private void processarCaminhoesGrandesOcupados() {
        for (int i = caminhoesGrandesOcupados.getTamanho() - 1; i >= 0; i--) {
            CaminhaoGrande caminhao = caminhoesGrandesOcupados.obter(i);
            // Atualiza estado do caminhão
            if (caminhao.atualizarEstado()) {
                // Caminhão retornou à estação, remove da lista
                caminhoesGrandesOcupados.remover(i);
                caminhoesGrandesDisponiveis.adicionar(caminhao);
                LoggerSimulacao.log("INFO", String.format("Caminhão grande %s disponível", caminhao.getPlaca()));
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
            LoggerSimulacao.log("CONFIG", String.format("Zona %s inicializada com intervalo de lixo [%d, %d]kg.", zona.getNome(), intervalosLixo[i][0], intervalosLixo[i][1]));
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

    protected static void inicializarEstacoes(int tempoMaxEspera, ZonaUrbana zonaEstacaoA, ZonaUrbana zonaEstacaoB) {
        estacoes.adicionar(new EstacaoTransferencia("Estação A", tempoMaxEspera, zonaEstacaoA));
        estacoes.adicionar(new EstacaoTransferencia("Estação B", tempoMaxEspera, zonaEstacaoB));
    }

    public static void inicializarAterro(ZonaUrbana zona) {
        zonaAterro = zona;
    }

    // Inicializa estatísticas das zonas para processamento de coletas
    private ZonaStats[] inicializarZonaStats() {
        ZonaStats[] zonaStats = new ZonaStats[zonas.getTamanho()];
        for (int i = 0; i < zonas.getTamanho(); i++) {
            zonaStats[i] = new ZonaStats(zonas.obter(i).getLixoAcumulado());
        }
        return zonaStats;
    }

    // Processa coletas de lixo pelos caminhões pequenos
    private void processarColetas(ZonaStats[] zonaStats) {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() != 2 || caminhao.getZonaAtual() == null) {
                continue;
            }
            // Processa tempo de coleta em andamento
            if (caminhao.getTempoColetaRestante() > 0) {
                if (caminhao.processarColeta()) {
                    // Coleta concluída, verifica se está cheio
                    if (caminhao.estaCheio()) {
                        enviarCaminhaoParaEstacao(caminhao);
                    }
                }
                continue;
            }
            // Tenta nova coleta se não está cheio
            if (!caminhao.estaCheio()) {
                processarColetaCaminhao(caminhao, zonaStats);
            }
        }
        processarCaminhoesEmTransito();
    }

    // Processa a coleta de um caminhão em uma zona
    private void processarColetaCaminhao(CaminhaoPequeno caminhao, ZonaStats[] zonaStats) {
        for (int j = 0; j < zonas.getTamanho(); j++) {
            if (zonas.obter(j) == caminhao.getZonaAtual()) {
                ZonaStats stats = zonaStats[j];
                stats.caminhoes++;
                stats.capacidadeTotal += caminhao.getCapacidade();
                // Calcula lixo disponível proporcional à capacidade
                int lixoDisponivel = stats.caminhoes > 0 && stats.capacidadeTotal > 0 ?
                        (int) ((double) caminhao.getCapacidade() / stats.capacidadeTotal * stats.lixoDisponivel) : 0;
                int coletado = caminhao.coletar(lixoDisponivel);
                if (coletado <= 0) {
                    stats.semColeta++;
                }
                stats.coletas.adicionar(coletado);
                break;
            }
        }
    }

    private void enviarCaminhaoParaEstacao(CaminhaoPequeno caminhao) {
        if (caminhao.estaCheio() && caminhao.getCargaAtual() > 0) {
            EstacaoTransferencia estacao = escolherEstacao(caminhao);
            if (estacao != null) {
                int tempoViagem = DistribuicaoCaminhoes.calcularTempoViagem(caminhao.getZonaAtual(), estacao.getZonaDaEstacao());
                caminhao.definirTempoViagem(tempoViagem);
                caminhao.setEstado(3); // EM_TRÂNSITO
                caminhao.getZonaAtual().decrementarCaminhoesAtivos();
                caminhao.setZonaDestino(null);
                caminhao.setEstacaoDestino(estacao);
                caminhao.viagensFeitas++;
                LoggerSimulacao.log("VIAGEM", String.format("Caminhão %s cheio, indo para %s (viagem: %dmin, carga: %dkg)",
                        caminhao.getPlaca(), estacao.getNome(), tempoViagem, caminhao.getCargaAtual()));
            }
        } else {
            // Se o caminhão tem 0 carga, torne-o disponível
            caminhao.setEstado(1); // DISPONÍVEL
            LoggerSimulacao.log("INFO", String.format("Caminhão %s não enviado à estação (carga: %dkg), agora disponível",
                    caminhao.getPlaca(), caminhao.getCargaAtual()));
        }
    }

    // Processa caminhões em trânsito para zonas ou estações
    private void processarCaminhoesEmTransito() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() == 3 && caminhao.processarViagem()) {
                // Verificando tempo de chegada
                int tempoEstimado = caminhao.getTempoViagemRestante();
                if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog() && tempoEstimado != 0) {
                    LoggerSimulacao.log("INFO", String.format("Caminhão %s chegou com discrepância de tempo: estimado=%dmin, restante=%dmin",
                            caminhao.getPlaca(), tempoEstimado, caminhao.getTempoViagemRestante()));
                }
                if (caminhao.getZonaDestino() != null) {
                    // Indo à zona
                    caminhao.getZonaAtual().decrementarCaminhoesAtivos(); // Diminui caminhões ativos na zona origem
                    caminhao.setZonaAtual(caminhao.getZonaDestino());
                    caminhao.setZonaDestino(null);
                    caminhao.setEstado(2); // COLETANDO
                    caminhao.getZonaAtual().incrementarCaminhoesAtivos();
                    LoggerSimulacao.log("CHEGADA", String.format("Caminhão %s chegou à zona %s.",
                            caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
                } else if (caminhao.getEstacaoDestino() != null) {
                    // Indo à estação
                    caminhao.setEstado(4); // FILA_ESTAÇÃO
                    caminhao.getEstacaoDestino().receberCaminhaoPequeno(caminhao, tempoSimulado);
                    LoggerSimulacao.log("CHEGADA", String.format("Caminhão %s chegou à %s, entrou na fila.",
                            caminhao.getPlaca(), caminhao.getEstacaoDestino().getNome()));
                }
            }
        }
    }

    private void logTentativasSemColeta(ZonaStats[] zonaStats) {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            if (zonaStats[i].semColeta > 0 && zonas.obter(i).getLixoAcumulado() == 0) {
                LoggerSimulacao.log("INFO", String.format("%d caminhão(ões) não conseguiu(ram) coletar em %s (sem lixo disponível).",
                        zonaStats[i].semColeta, zonas.obter(i).getNome()));
                zonaStats[i].semColeta = 0;
            }
        }
    }

    // Inicializa caminhões grandes, atribuindo diretamente às estações
    protected static void inicializarCaminhoesGrandes(int quantidade) {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (i < quantidade) {
                adicionarCaminhaoGrande(estacao); // Atribui caminhão grande à estação
            }
        }
    }

    private EstacaoTransferencia escolherEstacao(CaminhaoPequeno caminhao) {
        EstacaoTransferencia melhor = null;
        double melhorPontuacao = Double.MAX_VALUE; // menor custo = melhor

        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia est = estacoes.obter(i);
            int tamanhoFila = est.getFilaPequenos().getTamanho();
            int distancia = ZonaUrbana.getDistancia(est.getZonaDaEstacao().getNome(), caminhao.getZonaAtual().getNome());
            // Pesos teste
            double pontuacao = tamanhoFila * 10 + distancia * 5;

            if (pontuacao < melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhor = est;
            }
        }
        if (melhor == null) {
            LoggerSimulacao.log("ERRO", "Nenhuma estação adequada para o caminhão " + caminhao.getPlaca());
        }
        return melhor;
    }

    // Processa filas e caminhões grandes nas estações
    private void processarEstacoes() {
        int caminhoesGrandesEmUso = 0;
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            estacao.atualizarTempoEsperaCaminhaoGrande();
            // Libera caminhão grande se necessário
            CaminhaoGrande grandeLiberado = estacao.liberarCaminhaoGrandeSeNecessario();
            if (grandeLiberado != null) {
                adicionarCaminhaoGrandeOcupado(grandeLiberado); // Move para lista de ocupados
                estatisticas.liberarCaminhaoGrande(); // Decrementa contador
            }
            // Processa fila de caminhões pequenos ou transferência para caminhão grande
            ResultadoProcessamentoFila resultado = estacao.processarFila(tempoSimulado);
            if (resultado.foiProcessado()) {
                estatisticas.registrarEspera(resultado.getTempoDeEspera());
                CaminhaoPequeno pequeno = resultado.getCaminhaoProcessado();
                pequeno.setEstado(1); // DISPONÍVEL
                LoggerSimulacao.log("DESCARGA", String.format("Caminhão %s processado em %s, pode ser distribuído", pequeno.getPlaca(), estacao.getNome()));
            }
            // Atribui caminhão grande se necessário
            if (estacao.tempoEsperaExcedido() || estacao.getFilaPequenos().getTamanho() > 0) {
                if (!estacao.temCaminhaoGrandeFila()) {
                    CaminhaoGrande grande = null;
                    if (!caminhoesGrandesDisponiveis.estaVazia()) {
                        grande = caminhoesGrandesDisponiveis.remover(0);
                        estacao.atribuirCaminhaoGrande(grande);
                        estatisticas.registrarCaminhaoGrandeEmUso(); // Incrementa contador
                        LoggerSimulacao.log("INFO", String.format("Caminhão grande %s atribuído a %s", grande.getPlaca(), estacao.getNome()));
                    } else {
                        adicionarCaminhaoGrande(estacao);
                        estatisticas.registrarCaminhaoGrandeEmUso(); // Incrementa contador
                        LoggerSimulacao.log("INFO", String.format("Novo caminhão grande atribuído diretamente a %s", estacao.getNome()));
                    }
                }
            }
            if (estacao.temCaminhaoGrandeFila()) {
                caminhoesGrandesEmUso++;
            }
        }
        // Atualiza máximo de caminhões grandes em uso
        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);
    }

    // Adiciona um novo caminhão grande e o atribui diretamente a uma estação
    private static void adicionarCaminhaoGrande(EstacaoTransferencia estacao) {
        CaminhaoGrande novo = new CaminhaoGrande(toleranciaCaminhoesGrandes);
        todosCaminhoesGrandes.adicionar(novo); // Adiciona à lista de todos os caminhões grandes
        estatisticas.registrarNovoCaminhaoGrande();
        if (estacao != null) {
            estacao.atribuirCaminhaoGrande(novo);
        } else {
            caminhoesGrandesDisponiveis.adicionar(novo);
            LoggerSimulacao.log("INFO", String.format("Novo caminhão grande %s adicionado à lista de disponíveis.", novo.getPlaca()));
        }
    }

    // Adiciona um caminhão grande à lista de ocupados
    private static void adicionarCaminhaoGrandeOcupado(CaminhaoGrande caminhao) {
        caminhoesGrandesOcupados.adicionar(caminhao);
    }

    protected static void configurarSimuladorParams(int tolerancia) {
        setListaCaminhoesPequenos(caminhoesPequenos);
        setListaZonas(zonas);
        setListaEstacoes(estacoes);
        setToleranciaCaminhoesGrandes(tolerancia);
        inicializarCaminhoesGrandes(estacoes.getTamanho());
    }

    public static int getTempoSimulado() {
        return tempoSimulado;
    }

    public static int getCaminhoesPorZona() {
        return caminhoesPorZona;
    }

    protected static Lista<ZonaUrbana> getZonas() {
        return zonas;
    }

    public static ZonaUrbana getZonaAterro(){
        return zonaAterro;
    }

    protected static Lista<CaminhaoPequeno> getCaminhoesPequenos() {
        return caminhoesPequenos;
    }

    protected static Lista<CaminhaoGrande> getCaminhoesGrandes() {
        return todosCaminhoesGrandes;
    }

    // Classe interna para estatísticas de zonas durante coletas
    private static class ZonaStats {
        int lixoDisponivel;
        int caminhoes;
        int capacidadeTotal;
        int semColeta;
        Lista<Integer> coletas;

        ZonaStats(int lixoDisponivel) {
            this.lixoDisponivel = lixoDisponivel;
            this.caminhoes = 0;
            this.capacidadeTotal = 0;
            this.semColeta = 0;
            this.coletas = new Lista<>();
        }
    }
}


