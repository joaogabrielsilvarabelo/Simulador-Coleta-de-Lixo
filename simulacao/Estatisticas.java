package simulacao;

import estruturas.Lista;
import zonas.ZonaEstatistica;

import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

public class Estatisticas {
    private int totalLixoColetado;
    private int totalCaminhoesGrandesUsados;
    private int tempoTotalEsperaPequenos;
    private int descarregamentos;
    private int maxCaminhoesGrandesEmUso;
    private Lista<ZonaEstatistica> lixoPorZona;
    private int tempoSimulado;

    public Estatisticas() {
        this.lixoPorZona = new Lista<>();
        resetar();
    }

    public void resetar() {
        totalLixoColetado = 0;
        totalCaminhoesGrandesUsados = 0;
        tempoTotalEsperaPequenos = 0;
        descarregamentos = 0;
        maxCaminhoesGrandesEmUso = 0;
        tempoSimulado = 0;
        lixoPorZona = new Lista<>();
    }

    public void registrarColeta(int kg, String zona) {
        totalLixoColetado += kg;
        ZonaEstatistica estatistica = buscarZonaEstatistica(zona);
        if (estatistica == null) {
            estatistica = new ZonaEstatistica(zona);
            lixoPorZona.adicionar(estatistica);
        }
        estatistica.adicionarLixo(kg);
    }

    public void registrarNovoCaminhaoGrande() {
        totalCaminhoesGrandesUsados++;
    }

    public void registrarEspera(int minutos) {
        tempoTotalEsperaPequenos += minutos;
        descarregamentos++;
    }

    public void atualizarMaxCaminhoesGrandesEmUso(int emUso) {
        maxCaminhoesGrandesEmUso = Math.max(maxCaminhoesGrandesEmUso, emUso);
    }

    public void setTempoSimulado(int tempo) {
        this.tempoSimulado = tempo;
    }

    public void imprimirRelatorio() {
        System.out.println("===== Relatório de Simulação =====");
        System.out.println("Tempo simulado: " + formatarTempo(tempoSimulado));
        System.out.println("Lixo total coletado: " + totalLixoColetado + " kg");
        System.out.println("Por zona:");
        for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
            ZonaEstatistica est = lixoPorZona.obter(i);
            System.out.println("  - " + est.getNomeZona() + ": " + est.getLixoColetado() + " kg");
        }
        System.out.println("Caminhões grandes usados: " + totalCaminhoesGrandesUsados);
        System.out.println("Máximo de caminhões grandes em uso simultâneo: " + maxCaminhoesGrandesEmUso);
        System.out.println("Tempo médio de espera (CP): " +
                (descarregamentos > 0 ? tempoTotalEsperaPequenos / descarregamentos : 0) + " min");
    }

    public void salvarRelatorio(String arquivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new File(arquivo))) {
            writer.println("Relatório de Simulação");
            writer.println("Tempo simulado: " + formatarTempo(tempoSimulado));
            writer.println("Lixo total coletado: " + totalLixoColetado + " kg");
            writer.println("Por zona:");
            for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
                ZonaEstatistica est = lixoPorZona.obter(i);
                writer.println("  - " + est.getNomeZona() + ": " + est.getLixoColetado() + " kg");
            }
            writer.println("Caminhões grandes usados: " + totalCaminhoesGrandesUsados);
            writer.println("Máximo de caminhões grandes em uso simultâneo: " + maxCaminhoesGrandesEmUso);
            writer.println("Tempo médio de espera (CP): " +
                    (descarregamentos > 0 ? tempoTotalEsperaPequenos / descarregamentos : 0) + " min");
        }
    }

    private ZonaEstatistica buscarZonaEstatistica(String nomeZona) {
        for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
            ZonaEstatistica est = lixoPorZona.obter(i);
            if (est.getNomeZona().equals(nomeZona)) {
                return est;
            }
        }
        return null;
    }

    private String formatarTempo(int minutos) {
        int dias = minutos / (24 * 60);
        int horas = (minutos % (24 * 60)) / 60;
        int mins = minutos % 60;
        return String.format("Dia %d, %02d:%02d", dias + 1, horas, mins);
    }
}

