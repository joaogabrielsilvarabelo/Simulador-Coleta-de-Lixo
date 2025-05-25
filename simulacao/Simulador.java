package simulacao;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.DistribuicaoCaminhoes;
import estacoes.EstacaoTransferencia;
import estacoes.ResultadoProcessamentoFila;
import estruturas.Lista;
import zonas.ZonaUrbana;
import zonas.ZonaEstatistica;

// Gerencia a simulação de coleta e transporte de lixo
public class Simulador {
    private static int[][] intervalosLixo;
    private final DistribuicaoCaminhoes distribuicaoCaminhoes;
    private static Lista<CaminhaoPequeno> caminhoesPequenos;
    private static Lista<ZonaUrbana> zonas;
    private static Lista<EstacaoTransferencia> estacoes;
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

    // Chama os métodos necessários para começar um novo dia da simulação (geração de lixo e reinicio dos caminhões pequenos)
    private void concluirDia() {
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

    // Reativa caminhões pequenos que atingiram o limite de viagens
    private void reiniciarCaminhoesPequenos() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            caminhao.viagensFeitas = 0;
            if (caminhao.getEstado() == 6) { // ENCERRADO
                caminhao.setEstado(1); // DISPONÍVEL
                LoggerSimulacao.log("INFO", String.format("Caminhão %s teve suas viagens diárias resetadas e está reativado para novo dia.", caminhao.getPlaca()));
            }
            else {
                LoggerSimulacao.log("INFO", String.format("Caminhão %s teve suas viagens diárias resetadas.", caminhao.getPlaca()));
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
        // Inicializa estatísticas temporárias para cada zona
        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaEstatistica estatistica = estatisticas.buscarZonaEstatistica(zonas.obter(i).getNome());
            if (estatistica != null) {
                estatistica.resetarEstatisticasTemporarias();
            }
        }
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
                // Caminhão retornou à estação, remove da lista de ocupados
                caminhoesGrandesOcupados.remover(i);
                LoggerSimulacao.log("CHEGADA", String.format("Caminhão grande %s retornou à %s e está disponível novamente",
                        caminhao.getPlaca(), caminhao.getEstacaoOrigem().getNome()));
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

    // Processa coletas de lixo pelos caminhões pequenos
    private void processarColetas() {
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
                processarColetaCaminhao(caminhao);
            }
        }
    }

    // Processa a coleta de um caminhão em uma zona
    private void processarColetaCaminhao(CaminhaoPequeno caminhao) {
        for (int j = 0; j < zonas.getTamanho(); j++) {
            if (zonas.obter(j) == caminhao.getZonaAtual()) {
                ZonaEstatistica stats = estatisticas.buscarZonaEstatistica(zonas.obter(j).getNome());
                if (stats != null) {
                    stats.incrementarCaminhoes(caminhao.getCapacidade());
                    // Calcula lixo disponível proporcional à capacidade
                    int lixoDisponivel = stats.getCaminhoes() > 0 && stats.getCapacidadeTotal() > 0 ?
                            (int) ((double) caminhao.getCapacidade() / stats.getCapacidadeTotal() * zonas.obter(j).getLixoAcumulado()) : 0;
                    int coletado = caminhao.coletar(lixoDisponivel);
                    if (coletado <= 0) {
                        stats.registrarTentativaSemColeta();
                    }
                    stats.adicionarColeta(coletado);
                }
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
                    LoggerSimulacao.log("CHEGADA", String.format("Caminhão %s chegou à zona %s, iniciando coleta...",
                            caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
                } else if (caminhao.getEstacaoDestino() != null) {
                    // Indo à estação
                    caminhao.setEstado(4); // FILA_ESTAÇÃO
                    caminhao.getEstacaoDestino().receberCaminhaoPequeno(caminhao, tempoSimulado);
                }
            }
        }
    }

    private void logTentativasSemColeta() {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaEstatistica stats = estatisticas.buscarZonaEstatistica(zonas.obter(i).getNome());
            if (stats != null && stats.getSemColeta() > 0 && zonas.obter(i).getLixoAcumulado() == 0) {
                LoggerSimulacao.log("INFO", String.format("%d caminhão(ões) não conseguiu(ram) coletar em %s (sem lixo disponível)",
                        stats.getSemColeta(), zonas.obter(i).getNome()));
                stats.resetarEstatisticasTemporarias();
            }
        }
    }

    // Inicializa caminhões grandes, atribuindo diretamente às estações
    protected static void inicializarCaminhoesGrandes(int quantidade) {
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            if (i < quantidade) {
                adicionarCaminhaoGrande(estacao); // Adiciona caminhão grande à estação
            }
        }
    }

    private EstacaoTransferencia escolherEstacao(CaminhaoPequeno caminhao) {
        EstacaoTransferencia melhor = null;
        double melhorPontuacao = Double.MAX_VALUE; // Menor custo = melhor

        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia est = estacoes.obter(i);
            int tamanhoFila = est.getFilaPequenos().getTamanho();
            int distancia = ZonaUrbana.getDistancia(est.getZonaDaEstacao().getNome(), caminhao.getZonaAtual().getNome());
            // Fórmula: penaliza fila longa e distância
            double pontuacao = tamanhoFila * 10 + DistribuicaoCaminhoes.calcularTempoViagemBase(caminhao.getZonaAtual(), est.getZonaDaEstacao()) * 5;

            if (pontuacao < melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhor = est;
            }
        }
        if (melhor == null) {
            LoggerSimulacao.log("ERRO", "Nenhuma estação adequada para o caminhão " + caminhao.getPlaca());
        } else {
            LoggerSimulacao.log("INFO", String.format("Caminhão %s escolheu %s (fila: %d, pontuação: %.1f)",
                    caminhao.getPlaca(), melhor.getNome(), melhor.getFilaPequenos().getTamanho(), melhorPontuacao));
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
            // Processa fila de caminhões pequenos
            ResultadoProcessamentoFila resultado = estacao.processarFila(tempoSimulado);
            if (resultado.foiProcessado()) {
                estatisticas.registrarEspera(resultado.getTempoDeEspera());
                CaminhaoPequeno pequeno = resultado.getCaminhaoProcessado();
                pequeno.setEstado(1); // DISPONÍVEL
                LoggerSimulacao.log("DESCARGA", String.format("Caminhão %s processado em %s, pode ser distribuído", pequeno.getPlaca(), estacao.getNome()));
            }
            // Adiciona caminhão grande se necessário (fila ou espera excedida)
            if (estacao.tempoEsperaExcedido() || estacao.getFilaPequenos().getTamanho() > 0) {
                if (!estacao.temCaminhaoGrandeFila()) {
                    adicionarCaminhaoGrande(estacao); // Adiciona novo caminhão grande à estação
                    estacao.resetarEsperaTotalPequenos(); // Reseta tempos de espera
                    estatisticas.registrarCaminhaoGrandeEmUso(); // Incrementa contador
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
        estacao.atribuirCaminhaoGrande(novo);
        if (todosCaminhoesGrandes.getTamanho() <= 2) {
            LoggerSimulacao.logRelatorio("CONFIG", String.format("%s: Caminhão grande %s foi adicionado", estacao.getNome(), novo.getPlaca()));
        } else {
            LoggerSimulacao.log("ADIÇÃO",  String.format("%s: Caminhão grande %s foi adicionado (espera excedidia)", estacao.getNome(), novo.getPlaca()));
        }
    }

    // Adiciona um caminhão grande à lista de ocupados
    public static void adicionarCaminhaoGrandeOcupado(CaminhaoGrande caminhao) {
        caminhoesGrandesOcupados.adicionar(caminhao);
    }

    protected static void configurarSimuladorParams(int tolerancia) {
        setListaCaminhoesPequenos(caminhoesPequenos);
        setListaZonas(zonas);
        setListaEstacoes(estacoes);
        setToleranciaCaminhoesGrandes(tolerancia);
        inicializarCaminhoesGrandes(estacoes.getTamanho());
    }

    // Getters e Setters
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
}


