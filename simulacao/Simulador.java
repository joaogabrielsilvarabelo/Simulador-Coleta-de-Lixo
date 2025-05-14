package simulacao;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.DistribuicaoCaminhoes;
import estacoes.EstacaoTransferencia;
import estacoes.ResultadoProcessamentoFila;
import estruturas.Lista;
import zonas.ZonaUrbana;


public class Simulador {
    private final DistribuicaoCaminhoes distribuicaoCaminhoes;
    private Lista<CaminhaoPequeno> caminhoesPequenos;
    private Lista<ZonaUrbana> zonas;
    private Lista<EstacaoTransferencia> estacoes;
    private final Lista<CaminhaoGrande> caminhoesGrandesDisponiveis;
    private final Estatisticas estatisticas;
    private static int tempoSimulado;
    private int totalCaminhoesGrandesCriados;
    private int toleranciaCaminhoesGrandes;
    private boolean rodando;
    private boolean pausado;


    public Simulador() {
        this.caminhoesPequenos = new Lista<>();
        this.zonas = new Lista<>();
        this.estacoes = new Lista<>();
        this.caminhoesGrandesDisponiveis = new Lista<>();
        this.estatisticas = new Estatisticas();
        this.distribuicaoCaminhoes = new DistribuicaoCaminhoes();
        this.tempoSimulado = 0;
        this.totalCaminhoesGrandesCriados = 0;
        this.toleranciaCaminhoesGrandes = 0;
        this.rodando = false;
        this.pausado = false;
    }

    public void setListaZonas(Lista<ZonaUrbana> zonas) {
        this.zonas = zonas;
    }

    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> caminhoes) {
        this.caminhoesPequenos = caminhoes;
    }

    public void setListaEstacoes(Lista<EstacaoTransferencia> estacoes) {
        this.estacoes = estacoes;
    }

    public void setToleranciaCaminhoesGrandes(int tolerancia) {
        this.toleranciaCaminhoesGrandes = tolerancia;
    }

    public Estatisticas getEstatisticas() {
        return estatisticas;
    }

    public void iniciar() {
        if (!rodando) {
            rodando = true;
            pausado = false;
            log("Simulação iniciada.");
            new Thread(this::executarSimulacao).start();
        } else {
            log("Simulação já está rodando!");
        }
    }

    public void pausar() {
        if (rodando && !pausado) {
            pausado = true;
            log("Simulação pausada.");
        } else {
            log("Simulação não está rodando ou já está pausada!");
        }
    }

    public void continuarSimulacao() {
        if (rodando && pausado) {
            pausado = false;
            log("Simulação continuada.");
        } else {
            log("Simulação não está pausada ou não foi iniciada!");
        }
    }

    public void encerrar() {
        if (rodando) {
            rodando = false;
            pausado = false;
            log("Simulação encerrada.");
            estatisticas.imprimirRelatorio();
        }
    }

    private void executarSimulacao() {
        while (rodando) {
            if (!pausado) {
                atualizarSimulacao();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log("Erro na simulação: " + e.getMessage());
            }
        }
    }

    private void atualizarSimulacao() {
        tempoSimulado++;
        if (tempoSimulado % 60 == 0) {
            gerarLixoZonas();
            estatisticas.setTempoSimulado(tempoSimulado);
            estatisticas.imprimirRelatorio();
        }
        if (tempoSimulado % 10 == 0) {
            distribuirCaminhoesOciosos();
        }
        processarCaminhoesPequenos();
        processarEstacoes();
    }

    private void gerarLixoZonas() {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            zonas.obter(i).gerarLixo();
        }
    }

    public void disponibilizarCaminhoes(){
    }

    private void distribuirCaminhoesOciosos() {
        if (!zonas.estaVazia()) {
            int distribuidos = distribuicaoCaminhoes.distribuirCaminhoes(caminhoesPequenos, zonas);
            if (distribuidos > 0) {
                log(distribuidos + " caminhão(ões) distribuído(s).");
            } else {
                log("Nenhum caminhão distribuído.");
            }
        }
    }

    private void processarCaminhoesPequenos() {
        ZonaStats[] zonaStats = inicializarZonaStats();
        processarColetas(zonaStats);
        atualizarZonas(zonaStats);
        if (tempoSimulado % 60 == 0) {
            logTentativasSemColeta(zonaStats);
        }

        //Procura por caminhoes em zonas com 0 lixo e os torna disponível
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() == 2 && caminhao.getZonaAtual() != null && caminhao.getZonaAtual().getLixoAcumulado() == 0) {
                caminhao.setEstado(1); // DISPONÍVEL
                caminhao.getZonaAtual().decrementarCaminhoesAtivos(); // Reduz os caminhões ativos na zona
                log(String.format("Caminhão %s em %s (sem lixo) agora disponível.",
                        caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
            }
        }
        processarCaminhoesEmTransito();
    }

    private ZonaStats[] inicializarZonaStats() {
        ZonaStats[] zonaStats = new ZonaStats[zonas.getTamanho()];
        for (int i = 0; i < zonas.getTamanho(); i++) {
            zonaStats[i] = new ZonaStats(zonas.obter(i).getLixoAcumulado());
        }
        return zonaStats;
    }

    private void processarColetas(ZonaStats[] zonaStats) {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.limiteAtingido(caminhao.getViagensFeitas()) || caminhao.getEstado() != 2) {
                continue;
            }
            if (!caminhao.estaCheio() && caminhao.getZonaAtual() != null) {
                processarColetaCaminhao(caminhao, zonaStats);
            }
            if (caminhao.estaCheio()) {
                enviarCaminhaoParaEstacao(caminhao);
            }
        }
        processarCaminhoesEmTransito();
    }

    private void processarColetaCaminhao(CaminhaoPequeno caminhao, ZonaStats[] zonaStats) {
        for (int j = 0; j < zonas.getTamanho(); j++) {
            if (zonas.obter(j) == caminhao.getZonaAtual()) {
                ZonaStats stats = zonaStats[j];
                stats.caminhoes++;
                stats.capacidadeTotal += caminhao.getCapacidade();
                int lixoDisponivel = stats.caminhoes > 0 && stats.capacidadeTotal > 0 ?
                        (int) ((double) caminhao.getCapacidade() / stats.capacidadeTotal * stats.lixoDisponivel) : 0;
                int coletado = caminhao.coletar(lixoDisponivel);
                stats.coletas.adicionar(coletado);
                if (coletado > 0) {
                    log(String.format("Caminhão %s (estado: %s) coletou %dkg em %s, carga atual: %d/%dkg",
                            caminhao.getPlaca(), caminhao.determinarEstado(caminhao.getEstado()),
                            coletado, caminhao.getZonaAtual().getNome(), caminhao.getCargaAtual(), caminhao.getCapacidade()));
                    estatisticas.registrarColeta(coletado, caminhao.getZonaAtual().getNome());
                } else {
                    stats.semColeta++;
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
                caminhao.setZonaDestino(null); // Limpa zonaDestino
                caminhao.setEstacaoDestino(estacao);
                caminhao.viagensFeitas++;
                log(String.format("Caminhão %s cheio, indo para %s (viagem: %dmin, carga: %dkg)",
                        caminhao.getPlaca(), estacao.getNome(), tempoViagem, caminhao.getCargaAtual()));
            }
        } else {
            // Se o caminhão tem 0 carga, torne-o disponível
            caminhao.setEstado(1); // DISPONÍVEL
            log(String.format("Caminhão %s não enviado à estação (carga: %dkg), agora disponível",
                    caminhao.getPlaca(), caminhao.getCargaAtual()));
        }
    }

    private void processarCaminhoesEmTransito() {
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesPequenos.obter(i);
            if (caminhao.getEstado() == 3 && caminhao.processarViagem()) {
                if (caminhao.getZonaDestino() != null) {
                    // Indo à zona
                    caminhao.getZonaAtual().decrementarCaminhoesAtivos(); // Diminui caminhões ativos na zona origem
                    caminhao.setZonaAtual(caminhao.getZonaDestino());
                    caminhao.setZonaDestino(null);
                    caminhao.setEstado(2); // COLETANDO
                    caminhao.getZonaAtual().incrementarCaminhoesAtivos();
                    log(String.format("Caminhão %s chegou à zona %s, iniciando coleta",
                            caminhao.getPlaca(), caminhao.getZonaAtual().getNome()));
                } else if (caminhao.getEstacaoDestino() != null) {
                    // Indo à estação
                    caminhao.setEstado(4); // FILA_ESTAÇÃO
                    caminhao.getEstacaoDestino().receberCaminhaoPequeno(caminhao, tempoSimulado);
                    log(String.format("Caminhão %s chegou à %s, entrou na fila",
                            caminhao.getPlaca(), caminhao.getEstacaoDestino().getNome()));
                }
            }
        }
    }

    private void atualizarZonas(ZonaStats[] zonaStats) {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            int totalColetado = 0;
            Lista<Integer> coletas = zonaStats[i].coletas;
            for (int j = 0; j < coletas.getTamanho(); j++) {
                totalColetado += coletas.obter(j);
            }
            if (totalColetado > 0) {
                zonas.obter(i).coletarLixo(totalColetado);
            }
        }
    }

    private void logTentativasSemColeta(ZonaStats[] zonaStats) {
        for (int i = 0; i < zonas.getTamanho(); i++) {
            if (zonaStats[i].semColeta > 0 && zonas.obter(i).getLixoAcumulado() == 0) {
                log(String.format("%d caminhão(ões) não conseguiu(ram) coletar em %s (sem lixo disponível)",
                        zonaStats[i].semColeta, zonas.obter(i).getNome()));
                zonaStats[i].semColeta = 0;
            }
        }
    }

    public void inicializarCaminhoesGrandes(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            adicionarCaminhaoGrande();
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
            System.out.println("Nenhuma estação adequada para o caminhão " + caminhao.getPlaca());
        }
        return melhor;
    }

    private void processarEstacoes() {
        int caminhoesGrandesEmUso = 0;
        for (int i = 0; i < estacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = estacoes.obter(i);
            estacao.atualizarTempoEsperaCaminhaoGrande();
            CaminhaoGrande cgLiberado = estacao.liberarCaminhaoGrandeSeNecessario();
            if (cgLiberado != null) {
                caminhoesGrandesDisponiveis.adicionar(cgLiberado);
                log(String.format("Caminhão grande %s partiu de %s para o aterro devido à tolerância excedida.",
                        cgLiberado.getPlaca(), estacao.getNome()));
            }
            ResultadoProcessamentoFila resultado = estacao.processarFila(tempoSimulado);
            if (resultado.foiProcessado()) {
                estatisticas.registrarEspera(resultado.getTempoDeEspera());
                CaminhaoPequeno pequeno = resultado.getCaminhaoProcessado();
                pequeno.setEstado(1); // DISPONÍVEL
                log(String.format("Caminhão %s processado em %s, pode ser distribuído",
                        pequeno.getPlaca(), estacao.getNome()));
            }
            if (estacao.tempoEsperaExcedido() || estacao.getFilaPequenos().getTamanho() > 0) {
                if (!estacao.temCaminhaoGrandeFila() && !caminhoesGrandesDisponiveis.estaVazia()) {
                    CaminhaoGrande cg = caminhoesGrandesDisponiveis.remover(0);
                    estacao.atribuirCaminhaoGrande(cg);
                    log(String.format("Caminhão grande atribuído a %s", estacao.getNome()));
                } else if (!estacao.temCaminhaoGrandeFila()) {
                    log("Adicionando novo caminhão grande para " + estacao.getNome());
                    adicionarCaminhaoGrande();
                    CaminhaoGrande cg = caminhoesGrandesDisponiveis.remover(0);
                    estacao.atribuirCaminhaoGrande(cg);
                    log(String.format("Caminhão grande atribuído a %s", estacao.getNome()));
                }
            }
            if (estacao.temCaminhaoGrandeFila()) {
                caminhoesGrandesEmUso++;
            }
        }
        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);
    }

    private void adicionarCaminhaoGrande() {
        CaminhaoGrande novo = new CaminhaoGrande(null, toleranciaCaminhoesGrandes);
        caminhoesGrandesDisponiveis.adicionar(novo);
        totalCaminhoesGrandesCriados++;
        estatisticas.registrarNovoCaminhaoGrande();
    }

    private void log(String mensagem) {
        synchronized (System.out) {
            System.out.println(mensagem);
        }
    }

    public static int getTempoSimulado(){
        return tempoSimulado;
    }

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