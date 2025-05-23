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
    private int totalLixoAterro;
    private int caminhoesGrandesEmUsoAtual;

    public Estatisticas() {
        this.lixoPorZona = new Lista<>();
        resetar();
    }

    public void resetar() {
        totalLixoColetado = 0;
        totalLixoGerado = 0;
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

    public void registrarLixoAterro(int kg) {
        if (kg <= 0) {
            LoggerSimulacao.log("ERRO", "Tentativa de registrar quantidade inválida de lixo no aterro: " + kg + "kg");
            return;
        }
        totalLixoAterro += kg;
        LoggerSimulacao.log("INFO", String.format("Registrado %dkg de lixo enviado ao aterro. Total acumulado: %dkg", kg, totalLixoAterro));
    }

    public void registrarCaminhaoGrandeEmUso() {
        caminhoesGrandesEmUsoAtual++;
        maxCaminhoesGrandesEmUso = Math.max(maxCaminhoesGrandesEmUso, caminhoesGrandesEmUsoAtual);
    }

    public void liberarCaminhaoGrande() {
        caminhoesGrandesEmUsoAtual--;
    }

    public void imprimirRelatorio() {
        String tempoFormatado = LoggerSimulacao.formatarTempo(tempoSimulado);

        // Cabeçalho
        LoggerSimulacao.logRelatorio("CIANO_CLARO", String.format("=====  RELATÓRIO DA SIMULAÇÃO - %s ===== ", tempoFormatado));
        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Coleta por zona
        LoggerSimulacao.logRelatorio("VERDE", "[LIXO COLETADO POR ZONA]");
        for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
            ZonaEstatistica zona = lixoPorZona.obter(i);
            int toneladas = zona.getLixoColetado() / 1000;
            String grafico = gerarBarra(toneladas);
            LoggerSimulacao.logRelatorio("BRANCO", String.format("%-8s: %s%d toneladas%s %s%s%s", zona.getNomeZona(), LoggerSimulacao.CIANO, toneladas, LoggerSimulacao.RESET, LoggerSimulacao.VERDE, grafico, LoggerSimulacao.RESET));
        }

        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Resumo geral
        LoggerSimulacao.logRelatorio("AMARELO", "[RESUMO GERAL]");
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Lixo total gerado     : %d toneladas", totalLixoGerado / 1000));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Lixo total coletado   : %d toneladas", totalLixoColetado / 1000));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Lixo enviado ao aterro: %d toneladas", totalLixoAterro / 1000));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Tempo simulado        : %d minutos", tempoSimulado));
        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Caminhões grandes
        LoggerSimulacao.logRelatorio("MAGENTA", "[USO DE CAMINHÕES GRANDES]");
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Total utilizados                 : %d", totalCaminhoesGrandesUsados));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Máximo simultâneo em uso         : %d", maxCaminhoesGrandesEmUso));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Número mínimo necessário (estimado): %d", maxCaminhoesGrandesEmUso));
        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Tempo de espera
        LoggerSimulacao.logRelatorio("CIANO", "[DESEMPENHO NAS ESTAÇÕES]");
        double mediaEspera = descarregamentos > 0 ? (double) tempoTotalEsperaPequenos / descarregamentos : 0.0;
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Tempo médio de espera na fila: %.1f min", mediaEspera));
        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Caminhões pequenos
        LoggerSimulacao.logRelatorio("AMARELO", "[STATUS DOS CAMINHÕES PEQUENOS]");
        LoggerSimulacao.logRelatorio("BRANCO", String.format("%-4s %-8s %-10s %-12s %-35s %s", "Placa", "Zona", "Carga", "Status", "Atividade", "Viagens"));

        var caminhoesPequenos = Simulador.getCaminhoesPequenos();
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            var c = caminhoesPequenos.obter(i);
            String status = c.determinarEstado(c.getEstado());
            String atividade = switch (c.getEstado()) {
                case 2 -> "Coletando";
                case 3 -> "Em trânsito";
                case 4 -> "Na fila da estação";
                case 5 -> "Descarregando";
                case 6 -> "Encerrado";
                default -> "Disponível";
            };

            String corStatus = switch (c.getEstado()) {
                case 1 -> "VERDE";
                case 2, 3, 4, 5 -> "AZUL";
                case 6 -> "CINZA";
                default -> "VERMELHO";
            };

            String linha = String.format("%-8s %-8s %d ton /%d ton %-12s %-35s %d",
                    c.getPlaca(), c.getZonaAtual().getNome(),
                    c.getCargaAtual() / 1000, c.getCapacidade() / 1000,
                    status, atividade, c.getViagensFeitas());
            LoggerSimulacao.logRelatorio(corStatus, linha);
        }

        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Caminhões grandes
        LoggerSimulacao.logRelatorio("MAGENTA", "[STATUS DOS CAMINHÕES GRANDES]");
        LoggerSimulacao.logRelatorio("BRANCO", String.format("%-6s %-12s %-10s %-20s", "Placa", "Status", "Carga", "Estação Origem"));

        var caminhoesGrandes = Simulador.getCaminhoesGrandes();
        for (int i = 0; i < caminhoesGrandes.getTamanho(); i++) {
            var c = caminhoesGrandes.obter(i);
            String estado = switch (c.getEstado()) {
                case 0 -> "ESPERANDO";
                case 1 -> "INDO_ATERRO.";
                case 2 -> "DESCARREG.";
                case 3 -> "RETORNANDO";
                default -> "DESCONHECIDO";
            };
            String cor = switch (c.getEstado()) {
                case 0 -> "VERDE";
                case 1, 2, 3 -> "AZUL";
                default -> "VERMELHO";
            };
            String linha = String.format("%-6s %-12s %5d ton /%-5d ton %-20s",
                    c.getPlaca(), estado, c.getCargaAtual() / 1000, c.getCapacidade() / 1000,
                    c.getEstacaoOrigem() != null ? c.getEstacaoOrigem().getNome() : "-");
            LoggerSimulacao.logRelatorio(cor, linha);
        }
        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Fechamento
        LoggerSimulacao.logRelatorio("CIANO_CLARO", "==================================================");
    }

    private String gerarBarra(int toneladas) {
        int blocos = Math.min(toneladas / 5, 20);
        StringBuilder barra = new StringBuilder();
        for (int i = 0; i < blocos; i++) {
            barra.append("█");
        }
        return barra.toString();
    }

    public ZonaEstatistica buscarZonaEstatistica(String zona) {
        for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
            ZonaEstatistica estatistica = lixoPorZona.obter(i);
            if (estatistica.getNomeZona().equals(zona)) {
                return estatistica;
            }
        }
        return null;
    }

    public int getTotalLixoColetado() {
        return totalLixoColetado;
    }

    public int getTotalLixoAterro() {
        return totalLixoAterro;
    }

    public void salvarRelatorio(String arquivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new File(arquivo))) {
            String tempoFormatado = LoggerSimulacao.formatarTempo(tempoSimulado);

            // Cabeçalho
            writer.println(String.format("=====  RELATÓRIO DA SIMULAÇÃO - %s ===== ", tempoFormatado));
            writer.println();

            // Coleta por zona
            writer.println("[LIXO COLETADO POR ZONA]");
            for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
                ZonaEstatistica zona = lixoPorZona.obter(i);
                int toneladas = zona.getLixoColetado() / 1000;
                String grafico = gerarBarra(toneladas);
                writer.println(String.format("%-8s: %d toneladas %s", zona.getNomeZona(), toneladas, grafico));
            }

            writer.println();

            // Resumo geral
            writer.println("[RESUMO GERAL]");
            writer.println(String.format("Lixo total gerado     : %d toneladas", totalLixoGerado / 1000));
            writer.println(String.format("Lixo total coletado   : %d toneladas", totalLixoColetado / 1000));
            writer.println(String.format("Lixo enviado ao aterro: %d toneladas", totalLixoAterro / 1000));
            writer.println(String.format("Tempo simulado        : %d minutos", tempoSimulado));
            writer.println();

            // Caminhões grandes
            writer.println("[USO DE CAMINHÕES GRANDES]");
            writer.println(String.format("Total utilizados                 : %d", totalCaminhoesGrandesUsados));
            writer.println(String.format("Máximo simultâneo em uso         : %d", maxCaminhoesGrandesEmUso));
            writer.println(String.format("Número mínimo necessário (estimado): %d", maxCaminhoesGrandesEmUso));
            writer.println();

            // Tempo de espera
            writer.println("[DESEMPENHO NAS ESTAÇÕES]");
            double mediaEspera = descarregamentos > 0 ? (double) tempoTotalEsperaPequenos / descarregamentos : 0.0;
            writer.println(String.format("Tempo médio de espera na fila: %.1f min", mediaEspera));
            writer.println();

            // Caminhões pequenos
            writer.println("[STATUS DOS CAMINHÕES PEQUENOS]");
            writer.println(String.format("%-4s %-8s %-10s %-12s %-35s %s", "Placa", "Zona", "Carga", "Status", "Atividade", "Viagens"));
            var caminhoesPequenos = Simulador.getCaminhoesPequenos();
            for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
                var c = caminhoesPequenos.obter(i);
                String status = c.determinarEstado(c.getEstado());
                String atividade = switch (c.getEstado()) {
                    case 2 -> "Coletando";
                    case 3 -> "Em trânsito";
                    case 4 -> "Na fila da estação";
                    case 5 -> "Descarregando";
                    case 6 -> "Encerrado";
                    default -> "Disponível";
                };
                writer.println(String.format("%-8s %-8s %d ton /%-8d ton %-12s %-35s %d",
                        c.getPlaca(), c.getZonaAtual().getNome(),
                        c.getCargaAtual() / 1000, c.getCapacidade() / 1000,
                        status, atividade, c.getViagensFeitas()));
            }

            writer.println();

            // Caminhões grandes
            writer.println("[STATUS DOS CAMINHÕES GRANDES]");
            writer.println(String.format("%-6s %-12s %-10s %-20s", "Placa", "Status", "Carga", "Estação Origem"));
            var caminhoesGrandes = Simulador.getCaminhoesGrandes();
            for (int i = 0; i < caminhoesGrandes.getTamanho(); i++) {
                var c = caminhoesGrandes.obter(i);
                String estado = switch (c.getEstado()) {
                    case 0 -> "ESPERANDO";
                    case 1 -> "INDO_ATERRO.";
                    case 2 -> "DESCARREG.";
                    case 3 -> "RETORNANDO";
                    default -> "DESCONHECIDO";
                };
                writer.println(String.format("%-6s %-12s %5d /%-5d %-20s",
                        c.getPlaca(), estado, c.getCargaAtual(), c.getCapacidade(),
                        c.getEstacaoOrigem() != null ? c.getEstacaoOrigem().getNome() : "-"));
            }

            writer.println();

            // Fechamento
            writer.println("====================================================================");
        }
    }
}




