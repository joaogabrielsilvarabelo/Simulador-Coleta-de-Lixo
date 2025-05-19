package simulacao;

import estruturas.Lista;
import zonas.ZonaEstatistica;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Estatisticas {
    private int totalLixoColetado;
    private int totalLixoGerado;
    private int totalCaminhoesGrandesUsados;
    private int tempoTotalEsperaPequenos;
    private int descarregamentos;
    private int maxCaminhoesGrandesEmUso;
    private final Lista<ZonaEstatistica> lixoPorZona;
    private int tempoSimulado;
    // Total de lixo enviado ao aterro (em kg)
    private int totalLixoAterro;

    public Estatisticas() {
        this.lixoPorZona = new Lista<>();
        resetar();
    }

    // Reseta todas as estatísticas para uma nova simulação
    public void resetar() {
        totalLixoColetado = 0;
        totalLixoGerado = 0;
        // Reseta o total de lixo enviado ao aterro
        totalLixoAterro = 0;
        totalCaminhoesGrandesUsados = 0;
        tempoTotalEsperaPequenos = 0;
        descarregamentos = 0;
        maxCaminhoesGrandesEmUso = 0;
        tempoSimulado = 0;
        lixoPorZona.limpar();
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

    public void registrarGeracaoLixo(String zona, int kg) {
        totalLixoGerado += kg;
        ZonaEstatistica estatistica = buscarZonaEstatistica(zona);
        if (estatistica == null) {
            estatistica = new ZonaEstatistica(zona);
            lixoPorZona.adicionar(estatistica);
        }
        estatistica.adicionarLixoGerado(kg);
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

    // Registra a quantidade de lixo enviada ao aterro
    public void registrarLixoAterro(int kg) {
        if (kg <= 0) {
            LoggerSimulacao.log("ERRO", "Tentativa de registrar quantidade inválida de lixo no aterro: " + kg + "kg");
            return;
        }
        totalLixoAterro += kg;
    }

    // Imprime o relatório completo da simulação
    public void imprimirRelatorio() {
        System.out.println("===== Relatório de Simulação =====");
        System.out.println("Tempo simulado: " + formatarTempo(tempoSimulado));
        System.out.println("Lixo total gerado: " + totalLixoGerado + " kg");
        System.out.println("Lixo total coletado: " + totalLixoColetado + " kg");
        // Exibe o total de lixo enviado ao aterro
        System.out.println("Lixo total enviado ao aterro: " + totalLixoAterro + " kg");
        System.out.println("Por zona:");
        imprimirEstatisticasZonas();
        System.out.println("Caminhões grandes usados: " + totalCaminhoesGrandesUsados);
        System.out.println("Máximo de caminhões grandes em uso simultâneo: " + maxCaminhoesGrandesEmUso);
        System.out.println("Número mínimo de caminhões grandes necessários: " + maxCaminhoesGrandesEmUso);
        System.out.println("Tempo médio de espera (CP): " +
                (descarregamentos > 0 ? tempoTotalEsperaPequenos / descarregamentos : 0) + " min");
    }

    private void imprimirEstatisticasZonas() {
        for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
            ZonaEstatistica est = lixoPorZona.obter(i);
            System.out.println("  - " + est.getNomeZona() + ": " +
                    est.getLixoColetado() + " kg de " + est.getLixoGerado() + " kg");
        }
    }

    // Salva o relatório em um arquivo
    public void salvarRelatorio(String arquivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new File(arquivo))) {
            writer.println("Relatório de Simulação");
            writer.println("Tempo simulado: " + formatarTempo(tempoSimulado));
            writer.println("Lixo total gerado: " + totalLixoGerado + " kg");
            writer.println("Lixo total coletado: " + totalLixoColetado + " kg");
            // Inclui o total de lixo enviado ao aterro
            writer.println("Lixo total enviado ao aterro: " + totalLixoAterro + " kg");
            writer.println("Por zona:");
            for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
                ZonaEstatistica est = lixoPorZona.obter(i);
                writer.println("  - " + est.getNomeZona() + ": " + est.getLixoColetado() + " kg de " + est.getLixoGerado() + " kg");
            }
            writer.println("Caminhões grandes usados: " + totalCaminhoesGrandesUsados);
            writer.println("Máximo de caminhões grandes em uso simultâneo: " + maxCaminhoesGrandesEmUso);
            writer.println("Número mínimo de caminhões grandes necessários: " + maxCaminhoesGrandesEmUso);
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

    public int getTotalLixoColetado() {
        return totalLixoColetado;
    }

    public int getTotalLixoAterro() {
        return totalLixoAterro;
    }
}


