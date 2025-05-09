package simulacao;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.DistribuicaoCaminhoes;
import estacoes.EstacaoTransferencia;
import estacoes.ResultadoProcessamentoFila;
import estruturas.Lista;
import zonas.ZonaUrbana;
import java.util.Random;

public class Simulador {
    private DistribuicaoCaminhoes distribuicaoCaminhoes;
    private Lista<CaminhaoPequeno> todosOsCaminhoesPequenos;
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;
    private Lista<CaminhaoGrande> caminhoesGrandesDisponiveis;
    private Estatisticas estatisticas;
    private int tempoSimulado;
    private int totalCaminhoesGrandesCriados;
    private int toleranciaCaminhoesGrandes;
    private boolean rodando;
    private boolean pausado;

    private static final int PICO_MANHA_INICIO = 7 * 60;
    private static final int PICO_MANHA_FIM = 9 * 60 - 1;
    private static final int PICO_MEIO_DIA_INICIO = 12 * 60;
    private static final int PICO_MEIO_DIA_FIM = 13 * 60 - 1;
    private static final int PICO_TARDE_INICIO = 17 * 60;
    private static final int PICO_TARDE_FIM = 19 * 60 - 1;
    private static final int TEMPO_MIN_VIAGEM_NORMAL = 15;
    private static final int TEMPO_MAX_VIAGEM_NORMAL = 45;
    private static final int TEMPO_MIN_VIAGEM_PICO = 30;
    private static final int TEMPO_MAX_VIAGEM_PICO = 90;
    private static final int TEMPO_MINUTOS_POR_DIA = 24 * 60;

    public Simulador() {
        this.todosOsCaminhoesPequenos = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
        this.caminhoesGrandesDisponiveis = new Lista<>();
        this.estatisticas = new Estatisticas();
        this.distribuicaoCaminhoes = null;
        this.tempoSimulado = 0;
        this.totalCaminhoesGrandesCriados = 0;
        this.toleranciaCaminhoesGrandes = 0;
        this.rodando = false;
        this.pausado = false;
    }

    public void setListaZonas(Lista<ZonaUrbana> zonas) {
        this.listaZonas = zonas;
        if (zonas != null && !zonas.estaVazia()) {
            this.distribuicaoCaminhoes = new DistribuicaoCaminhoes();
        }
    }

    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> caminhoes) {
        this.todosOsCaminhoesPequenos = caminhoes;
    }

    public void setListaEstacoes(Lista<EstacaoTransferencia> estacoes) {
        this.listaEstacoes = estacoes;
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
            System.out.println("Simulação iniciada.");
            new Thread(this::executarSimulacao).start();
        } else {
            System.out.println("Simulação já está rodando!");
        }
    }

    public void pausar() {
        if (rodando && !pausado) {
            pausado = true;
            System.out.println("Simulação pausada.");
        } else {
            System.out.println("Simulação não está rodando ou já está pausada!");
        }
    }

    public void continuarSimulacao() {
        if (rodando && pausado) {
            pausado = false;
            System.out.println("Simulação continuada.");
        } else {
            System.out.println("Simulação não está pausada ou não foi iniciada!");
        }
    }

    public void encerrar() {
        if (rodando) {
            rodando = false;
            pausado = false;
            System.out.println("Simulação encerrada.");
            estatisticas.imprimirRelatorio();
        }
    }

    private void executarSimulacao() {
        while (rodando) {
            if (!pausado) {
                atualizarSimulacao();
            }
            try {
                Thread.sleep(100); // Simula 1 minuto a cada 100ms
            } catch (InterruptedException e) {
                System.out.println("Erro na simulação: " + e.getMessage());
            }
        }
    }

    private void atualizarSimulacao() {
        tempoSimulado++;
        if (tempoSimulado % 60 == 0) {
            for (int i = 0; i < listaZonas.getTamanho(); i++) {
                listaZonas.obter(i).gerarLixo();
            }
            estatisticas.setTempoSimulado(tempoSimulado);
            estatisticas.imprimirRelatorio();
        }
        distribuirCaminhoesOciosos();
        processarCaminhoesPequenos();
        processarEstacoes();
    }

    private void distribuirCaminhoesOciosos() {
        if (distribuicaoCaminhoes != null && !listaZonas.estaVazia()) {
            int distribuidos = distribuicaoCaminhoes.distribuirCaminhoes(todosOsCaminhoesPequenos, listaZonas);
            if (distribuidos > 0) {
                System.out.println(distribuidos + " caminhões distribuídos.");
            }
        }
    }

    private boolean isHorarioDePico(int tempoMinutos) {
        int minutosNoDia = tempoMinutos % TEMPO_MINUTOS_POR_DIA;
        return (minutosNoDia >= PICO_MANHA_INICIO && minutosNoDia <= PICO_MANHA_FIM) ||
                (minutosNoDia >= PICO_MEIO_DIA_INICIO && minutosNoDia <= PICO_MEIO_DIA_FIM) ||
                (minutosNoDia >= PICO_TARDE_INICIO && minutosNoDia <= PICO_TARDE_FIM);
    }

    private int calcularTempoViagem(ZonaUrbana origem, ZonaUrbana destino) {
        int minutosNoDia = tempoSimulado % TEMPO_MINUTOS_POR_DIA;
        int min, max, variacao;
        if (isHorarioDePico(minutosNoDia)) {
            min = TEMPO_MIN_VIAGEM_PICO;
            max = TEMPO_MAX_VIAGEM_PICO;
            variacao = origem.getVariacaoPico() + destino.getVariacaoPico();
        } else {
            min = TEMPO_MIN_VIAGEM_NORMAL;
            max = TEMPO_MAX_VIAGEM_NORMAL;
            variacao = origem.getVariacaoNormal() + destino.getVariacaoNormal();
        }
        return Math.max(1, new Random().nextInt(max - min + 1) + min + variacao);
    }

    private void processarCaminhoesPequenos() {
        for (int i = 0; i < todosOsCaminhoesPequenos.getTamanho(); i++) {
            CaminhaoPequeno caminhao = todosOsCaminhoesPequenos.obter(i);
            if (caminhao.limiteAtingido(caminhao.getViagensFeitas())) {
                continue;
            }
            if (caminhao.getEstado() == 2) { // COLETANDO
                if (!caminhao.estaCheio() && caminhao.getZonaAtual() != null) {
                    int lixoDisponivel = caminhao.getZonaAtual().getLixoAcumulado();
                    int coletado = caminhao.coletar(lixoDisponivel);
                    System.out.printf("Caminhão %s (estado: %s) tentou coletar %dkg em %s, coletou %dkg, carga atual: %d/%dkg%n",
                            caminhao.getPlaca(), caminhao.determinarEstado(caminhao.getEstado()),
                            lixoDisponivel, caminhao.getZonaAtual().getNome(), coletado, caminhao.getCargaAtual(), caminhao.getCapacidade());
                    if (coletado > 0) {
                        caminhao.getZonaAtual().coletarLixo(coletado);
                        estatisticas.registrarColeta(coletado, caminhao.getZonaAtual().getNome());
                    }
                }
                if (caminhao.estaCheio()) {
                    EstacaoTransferencia estacao = escolherEstacao();
                    if (estacao != null) {
                        int tempoViagem = calcularTempoViagem(caminhao.getZonaAtual(), caminhao.getZonaBase());
                        caminhao.definirTempoViagem(tempoViagem);
                        caminhao.setEstado(3); // INDO_ESTAÇÃO
                        caminhao.setZonaAtual(null);
                        caminhao.setEstacaoDestino(estacao);
                        caminhao.viagensFeitas++;
                        System.out.printf("Caminhão %s cheio, indo para %s (viagem: %dmin)%n",
                                caminhao.getPlaca(), estacao.getNome(), tempoViagem);
                    }
                }
            } else if (caminhao.getEstado() == 3) { // INDO_ESTAÇÃO
                if (caminhao.processarViagem()) {
                    caminhao.setEstado(4); // FILA_ESTAÇÃO
                    caminhao.getEstacaoDestino().receberCaminhaoPequeno(caminhao, tempoSimulado);
                    System.out.printf("Caminhão %s chegou à %s, entrou na fila%n",
                            caminhao.getPlaca(), caminhao.getEstacaoDestino().getNome());
                }
            }
        }
    }

    private EstacaoTransferencia escolherEstacao() {
        EstacaoTransferencia melhor = null;
        int menorFila = Integer.MAX_VALUE;
        for (int i = 0; i < listaEstacoes.getTamanho(); i++) {
            EstacaoTransferencia est = listaEstacoes.obter(i);
            int tamanhoFila = est.getFilaPequenos().getTamanho();
            if (tamanhoFila < menorFila) {
                menorFila = tamanhoFila;
                melhor = est;
            }
        }
        return melhor;
    }

    private void processarEstacoes() {
        int caminhoesGrandesEmUso = 0;
        for (int i = 0; i < listaEstacoes.getTamanho(); i++) {
            EstacaoTransferencia estacao = listaEstacoes.obter(i);
            ResultadoProcessamentoFila resultado = estacao.processarFila(tempoSimulado);
            if (resultado.foiProcessado()) {
                estatisticas.registrarEspera(resultado.getTempoDeEspera());
                CaminhaoPequeno cp = resultado.getCaminhaoProcessado();
                cp.setEstado(1); // DISPONÍVEL
                cp.setZonaAtual(cp.getZonaBase());
                System.out.printf("Caminhão %s processado em %s, voltou para %s%n",
                        cp.getPlaca(), estacao.getNome(), cp.getZonaBase().getNome());
            }
            if (estacao.tempoEsperaExcedido()) {
                System.out.println("Adicionando novo caminhão grande devido a espera excessiva em " + estacao.getNome());
                adicionarCaminhaoGrande();
            }
            if (estacao.precisaCaminhaoGrande() && caminhoesGrandesDisponiveis.getTamanho() > 0) {
                CaminhaoGrande cg = caminhoesGrandesDisponiveis.remover(0);
                estacao.atribuirCaminhaoGrande(cg);
                System.out.printf("Caminhão grande atribuído a %s%n", estacao.getNome());
            }
            if (estacao.temCaminhaoGrandeFila()) {
                caminhoesGrandesEmUso++;
            }
        }
        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);
    }

    private void adicionarCaminhaoGrande() {
        CaminhaoGrande novo = new CaminhaoGrande();
        caminhoesGrandesDisponiveis.adicionar(novo);
        totalCaminhoesGrandesCriados++;
        estatisticas.registrarNovoCaminhaoGrande();
    }
}