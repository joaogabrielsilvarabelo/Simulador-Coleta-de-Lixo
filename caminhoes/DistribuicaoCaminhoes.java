package caminhoes;

import estruturas.Lista;
import zonas.ZonaUrbana;

public class DistribuicaoCaminhoes {
    private static final int LIMITE_CAMINHOES_POR_ZONA = 3;
    private static final int DISTANCIA_MAXIMA = 50;

    public int distribuirCaminhoes(Lista<CaminhaoPequeno> caminhoes, Lista<ZonaUrbana> zonas) {
        int distribuidos = 0;
        for (int i = 0; i < caminhoes.getTamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoes.obter(i);
            // Verifica se o caminhão está disponível e não atingiu o limite de viagens
            if (caminhao.getEstado() == 1 && //DISPONÍVEL
                    !caminhao.limiteAtingido(caminhao.getViagensFeitas())) {
                ZonaUrbana melhorZona = encontrarMelhorZona(caminhao, zonas);
                if (melhorZona != null) {
                    caminhao.setZonaAtual(melhorZona);
                    caminhao.setEstado(2); //COLETANDO
                    melhorZona.incrementarCaminhoesAtivos();
                    System.out.printf("Caminhão %s enviado para %s (lixo: %dkg)%n",
                            caminhao.getPlaca(), melhorZona.getNome(), melhorZona.getLixoAcumulado());
                    distribuidos++;
                }
            }
        }
        return distribuidos;
    }

    private ZonaUrbana encontrarMelhorZona(CaminhaoPequeno caminhao, Lista<ZonaUrbana> zonas) {
        ZonaUrbana melhorZona = null;
        int maiorLixo = -1;
        ZonaUrbana zonaBase = caminhao.getZonaBase() != null ? caminhao.getZonaBase() : zonas.obter(0);

        for (int i = 0; i < zonas.getTamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            int lixo = zona.getLixoAcumulado();
            int distancia = ZonaUrbana.getDistancia(zona.getNome(), zonaBase.getNome());
            // Regras: maior lixo, dentro da distância, não lotada
            if (lixo > maiorLixo &&
                    distancia <= DISTANCIA_MAXIMA &&
                    zona.getCaminhoesAtivos() < LIMITE_CAMINHOES_POR_ZONA) {
                maiorLixo = lixo;
                melhorZona = zona;
            }
        }
        return melhorZona;
    }
}
