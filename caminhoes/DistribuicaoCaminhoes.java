package caminhoes;

import estruturas.Lista;
import simulacao.InterfaceSimulador;
import simulacao.Simulador;
import zonas.ZonaUrbana;

import java.util.Random;

public class DistribuicaoCaminhoes {
    private final int limiteCaminhoesPorZona = calcularLimiteCaminhoesPorZona();
    private static final int DISTANCIA_MAXIMA = 50;
    private static final int caminhoesPorZona = InterfaceSimulador.getCaminhoesPorZona();

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
                if (melhorZona != null && melhorZona != caminhao.getZonaAtual()) { // Evita redistribuir à mesma zona
                    int tempoViagem = calcularTempoViagem(caminhao.getZonaAtual(), melhorZona);
                    caminhao.definirTempoViagem(tempoViagem);
                    caminhao.setEstado(3); // EM_TRÂNSITO
                    caminhao.setZonaDestino(melhorZona); // Seta a zona de destino
                    distribuidos++;
                    System.out.printf("Caminhão %s será redistribuído de %s para %s (viagem: %dmin)%n",
                            caminhao.getPlaca(), caminhao.getZonaAtual().getNome(), melhorZona.getNome(), tempoViagem);
                }
            }
        }
        System.out.println("Caminhões disponíveis: " + contarCaminhoesDisponiveis(caminhoes) + ", zonas com lixo: " + contarZonasComLixo(zonas));
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
            System.out.println("Erro: Origem ou destino nulo. Usando tempo padrão.");
            return TEMPO_MIN_VIAGEM_NORMAL;
        }
        int minutosNoDia = Simulador.getTempoSimulado() % TEMPO_MINUTOS_POR_DIA;
        int min = isHorarioDePico(minutosNoDia) ? TEMPO_MIN_VIAGEM_PICO : TEMPO_MIN_VIAGEM_NORMAL;
        int max = isHorarioDePico(minutosNoDia) ? TEMPO_MAX_VIAGEM_PICO : TEMPO_MAX_VIAGEM_NORMAL;
        int variacao = isHorarioDePico(minutosNoDia) ?
                origem.getVariacaoPico() + destino.getVariacaoPico() :
                origem.getVariacaoNormal() + destino.getVariacaoNormal();
        return Math.max(1, new Random().nextInt(max - min + 1) + min + variacao);
    }

    private boolean isCaminhaoDisponivel(CaminhaoPequeno caminhao) {
        boolean disponivel = caminhao.getEstado() == 1 && !caminhao.limiteAtingido(caminhao.getViagensFeitas());
        if (!disponivel) {
            System.out.println("Caminhão " + caminhao.getPlaca() + " não disponível: estado=" + caminhao.determinarEstado(caminhao.getEstado()) + ", viagens=" + caminhao.getViagensFeitas());
        }
        return disponivel;
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

        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            if (isZonaValida(zona, zonaAtual) && zona != zonaAtual) { // Exclui a zona atual
                double pontuacao = calcularPontuacao(zona, zonaAtual);
                System.out.println("Zona " + zona.getNome() + ": lixo=" + zona.getLixoAcumulado() + "kg, distância=" + ZonaUrbana.getDistancia(zona.getNome(), zonaAtual.getNome()) + "km, pontuação=" + pontuacao);
                if (pontuacao > melhorPontuacao) {
                    melhorPontuacao = pontuacao;
                    melhorZona = zona;
                }
            } else {
                System.out.println("Zona " + zona.getNome() + " rejeitada: lixo=" + zona.getLixoAcumulado() +
                        ", caminhões ativos=" + zona.getCaminhoesAtivos() +
                        ", distância=" + ZonaUrbana.getDistancia(zona.getNome(), zonaAtual.getNome()));
            }
        }
        if (melhorZona == null) {
            System.out.println("Nenhuma zona válida para caminhão " + caminhao.getPlaca());
        } else {
            System.out.println("Melhor zona para caminhão " + caminhao.getPlaca() + ": " + melhorZona.getNome() + " (lixo=" + melhorZona.getLixoAcumulado() + "kg)");
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
            double penalidadeCaminhoes = (caminhoesAtivos - limiteCaminhoesPorZona) * 100;
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

    public void atribuirCaminhaoAZona(CaminhaoPequeno caminhao, ZonaUrbana zona) {
        if (caminhao.getZonaAtual() != null) {
            caminhao.getZonaAtual().decrementarCaminhoesAtivos();
        }
        caminhao.setZonaAtual(zona);
        caminhao.setEstado(2); // COLETANDO
        zona.incrementarCaminhoesAtivos();
        caminhao.viagensFeitas++;
        System.out.printf("Caminhão %s enviado para %s (lixo: %dkg)%n",
                caminhao.getPlaca(), zona.getNome(), zona.getLixoAcumulado());
    }
}
