package caminhoes;

import estruturas.Lista;
import simulacao.LoggerSimulacao;
import simulacao.Simulador;
import zonas.ZonaUrbana;

import java.util.Random;

public class DistribuicaoCaminhoes {
    private final int limiteCaminhoesPorZona = calcularLimiteCaminhoesPorZona();
    private static final int DISTANCIA_MAXIMA = 50;
    private static final int caminhoesPorZona = Simulador.getCaminhoesPorZona();

    private static final int PICO_MANHA_INICIO = 7 * 60;
    private static final int PICO_MANHA_FIM = 9 * 60 - 1;
    private static final int PICO_MEIO_DIA_INICIO = 12 * 60;
    private static final int PICO_MEIO_DIA_FIM = 13 * 60 - 1;
    private static final int PICO_TARDE_INICIO = 17 * 60;
    private static final int PICO_TARDE_FIM = 19 * 60 - 1;
    private static final int TEMPO_MINUTOS_POR_DIA = 24 * 60;
    // Velocidades médias em km/h
    private static final double VELOCIDADE_MEDIA_NORMAL = 30.0; // Fora de pico
    private static final double VELOCIDADE_MEDIA_PICO = 20.0;   // Em pico
    // Margem de aleatoriedade (+/- 10%)
    private static final double MARGEM_ALEATORIA = 0.1;

    private int calcularLimiteCaminhoesPorZona() {
        // Ajuste dinâmico: +1 para permitir flexibilidade em zonas com muito lixo
        return Math.max(2, caminhoesPorZona + 1);
    }

    public int distribuirCaminhoes(Lista<CaminhaoPequeno> caminhoes, Lista<ZonaUrbana> zonas) {
        int distribuidos = 0;
        for (int i = 0; i < caminhoes.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoes.obter(i);
            if (isCaminhaoDisponivel(caminhao)) {
                ZonaUrbana melhorZona = encontrarMelhorZona(caminhao, zonas);
                ZonaUrbana zonaAtual = caminhao.getZonaAtual();
                // Se todas as zonas foram rejeitadas, é provavelmente porque todas tiveram o lixo coletado pelo dia
                if (melhorZona == null) {
                    LoggerSimulacao.log("INFO", "Caminhão " + caminhao.getPlaca() + " não tem destino para viajar, será determinado como ENCERRADO pelo dia");
                    caminhao.setEstado(6); // ENCERRADO
                } else if (melhorZona != zonaAtual) {
                    int tempoViagem = calcularTempoViagem(caminhao.getZonaAtual(), melhorZona);
                    caminhao.definirTempoViagem(tempoViagem);
                    caminhao.setEstado(3); // EM_TRÂNSITO
                    caminhao.setZonaDestino(melhorZona);
                    distribuidos++;
                    LoggerSimulacao.log("INFO", String.format("Caminhão %s será redistribuído de %s para %s (viagem: %dmin)",
                            caminhao.getPlaca(), caminhao.getZonaAtual().getNome(), melhorZona.getNome(), tempoViagem));
                } else {
                    LoggerSimulacao.log("INFO", "Caminhão " + caminhao.getPlaca() + " permanecerá na Zona " + caminhao.getZonaAtual().getNome() + " e começará a coletar");
                    caminhao.setEstado(2); // COLETANDO
                }
            } else if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
                // Loga caminhões não disponíveis apenas no modo DEBUG
                LoggerSimulacao.log("INFO", "Caminhão " + caminhao.getPlaca() + " não disponível: estado=" + caminhao.determinarEstado(caminhao.getEstado()) + ", viagens=" + caminhao.getViagensFeitas());
            }
        }
        if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
            LoggerSimulacao.log("INFO", "Caminhões disponíveis: " + contarCaminhoesDisponiveis(caminhoes) + ", zonas com lixo: " + contarZonasComLixo(zonas));
        }
        return distribuidos;
    }

    private static boolean isHorarioDePico(int tempoMinutos) {
        int minutosNoDia = tempoMinutos % TEMPO_MINUTOS_POR_DIA;
        return (minutosNoDia >= PICO_MANHA_INICIO && minutosNoDia <= PICO_MANHA_FIM) ||
                (minutosNoDia >= PICO_MEIO_DIA_INICIO && minutosNoDia <= PICO_MEIO_DIA_FIM) ||
                (minutosNoDia >= PICO_TARDE_INICIO && minutosNoDia <= PICO_TARDE_FIM);
    }

    public static int calcularTempoViagem(ZonaUrbana origem, ZonaUrbana destino) {
        if (origem == null || destino == null) {
            LoggerSimulacao.log("ERRO", "Origem ou destino nulo. Usando tempo padrão.");
            return 15; // Tempo padrão em minutos
        }
        // Obtém a distância entre as zonas
        int distancia = ZonaUrbana.getDistancia(origem.getNome(), destino.getNome());
        if (distancia == Integer.MAX_VALUE) {
            LoggerSimulacao.log("ERRO", "Distância não encontrada entre " + origem.getNome() + " e " + destino.getNome() + ". Usando tempo padrão.");
            return 15;
        }
        // Determina a velocidade média com base no horário
        int minutosNoDia = Simulador.getTempoSimulado() % TEMPO_MINUTOS_POR_DIA;
        boolean isPico = isHorarioDePico(minutosNoDia);
        double velocidadeMedia = isPico ? VELOCIDADE_MEDIA_PICO : VELOCIDADE_MEDIA_NORMAL;
        // Calcula o tempo base em minutos (distância / velocidade * 60)
        double tempoBase = (distancia / velocidadeMedia) * 60;
        // Adiciona a variação da zona (pico ou normal) diretamente em minutos
        int variacao = isPico ? (origem.getVariacaoPico() + destino.getVariacaoPico()) :
                (origem.getVariacaoNormal() + destino.getVariacaoNormal());
        double tempoAjustado = tempoBase + variacao;
        // Adiciona pequena aleatoriedade (+/- 10%)
        Random random = new Random();
        double fatorAleatorio = 1.0 + (random.nextDouble() * 2 * MARGEM_ALEATORIA - MARGEM_ALEATORIA);
        int tempoFinal = (int) Math.max(1, Math.round(tempoAjustado * fatorAleatorio));
        // Loga detalhes em modo DEBUG
        if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
            LoggerSimulacao.log("INFO", String.format("Tempo de viagem de %s para %s: distância=%dkm, velocidade=%.1fkm/h, variação=%dmin, aleatoriedade=%.2f, tempo=%dmin",
                    origem.getNome(), destino.getNome(), distancia, velocidadeMedia, variacao, fatorAleatorio, tempoFinal));
        }
        return tempoFinal;
    }

    private boolean isCaminhaoDisponivel(CaminhaoPequeno caminhao) {
        return caminhao.getEstado() == 1;
    }

    private int contarCaminhoesDisponiveis(Lista<CaminhaoPequeno> caminhoes) {
        int disponiveis = 0;
        for (int i = 0; i < caminhoes.getTamanho(); i++) {
            if (isCaminhaoDisponivel(caminhoes.obter(i))) {
                disponiveis++;
            }
        }
        return disponiveis;
    }

    private ZonaUrbana encontrarMelhorZona(CaminhaoPequeno caminhao, Lista<ZonaUrbana> zonas) {
        ZonaUrbana melhorZona = null;
        double melhorPontuacao = -1;
        ZonaUrbana zonaAtual = caminhao.getZonaAtual();
        // Avaliando zonas disponíveis para o caminhão
        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            if (isZonaValida(zona, zonaAtual)) {
                double pontuacao = calcularPontuacao(zona, zonaAtual);
                // Logando detalhes de pontuação apenas no modo DEBUG
                if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
                    LoggerSimulacao.log("INFO", String.format("Zona %s: lixo=%dkg, distância=%dkm, pontuação=%.1f",
                            zona.getNome(), zona.getLixoAcumulado(), ZonaUrbana.getDistancia(zona.getNome(), zonaAtual.getNome()), pontuacao));
                }
                if (pontuacao > melhorPontuacao) {
                    melhorPontuacao = pontuacao;
                    melhorZona = zona;
                }
            } else if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
                // Logando zonas rejeitadas apenas no modo DEBUG
                LoggerSimulacao.log("INFO", String.format("Zona %s rejeitada: lixo=%dkg, caminhões ativos=%d, distância=%d",
                        zona.getNome(), zona.getLixoAcumulado(), zona.getCaminhoesAtivos(), ZonaUrbana.getDistancia(zona.getNome(), zonaAtual.getNome())));
            }
        }
        // Logando a melhor zona escolhida
        if (melhorZona == null) {
            LoggerSimulacao.log("INFO", "Nenhuma zona válida para caminhão " + caminhao.getPlaca());
        } else {
            LoggerSimulacao.log("INFO", String.format("Melhor zona para caminhão %s: %s (lixo=%dkg)",
                    caminhao.getPlaca(), melhorZona.getNome(), melhorZona.getLixoAcumulado()));
        }
        return melhorZona;
    }

    private boolean isZonaValida(ZonaUrbana zona, ZonaUrbana zonaAtual) {
        int lixo = zona.getLixoAcumulado();
        int distancia = ZonaUrbana.getDistancia(zona.getNome(), zonaAtual.getNome());
        boolean valida = lixo > 0 && distancia <= DISTANCIA_MAXIMA;
        return valida;
    }

    private double calcularPontuacao(ZonaUrbana zona, ZonaUrbana zonaAtual) {
        int lixo = zona.getLixoAcumulado();
        int distancia = ZonaUrbana.getDistancia(zona.getNome(), zonaAtual.getNome());
        int caminhoesAtivos = zona.getCaminhoesAtivos();
        double pontuacao = lixo * 0.8 - distancia * 15;
        if (caminhoesAtivos > limiteCaminhoesPorZona) {
            double penalidadeCaminhoes = (caminhoesAtivos - limiteCaminhoesPorZona) * 200;
            pontuacao -= penalidadeCaminhoes;
        }
        return pontuacao;
    }

    private int contarZonasComLixo(Lista<ZonaUrbana> zonas) {
        int count = 0;
        for (int i = 0; i < zonas.getTamanho(); i++) {
            if (zonas.obter(i).getLixoAcumulado() > 0) {
                count++;
            }
        }
        return count;
    }
}

