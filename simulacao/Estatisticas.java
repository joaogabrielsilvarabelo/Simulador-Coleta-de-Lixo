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
        // Resumo geral
        LoggerSimulacao.logRelatorio("BRANCO", "[RESUMO GERAL]");
        double progressoTotal = totalLixoGerado > 0 ? (double) totalLixoColetado / totalLixoGerado * 100 : 0.0;
        String barraProgressoTotal = gerarBarraProgresso(progressoTotal);
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Tempo simulado:            %d minutos", tempoSimulado));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Lixo total gerado:         %d toneladas", totalLixoGerado / 1000));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Lixo total coletado:       %d toneladas", totalLixoColetado / 1000));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Progresso de coleta total: %5.1f%% %s", progressoTotal, barraProgressoTotal));
        LoggerSimulacao.logRelatorio("BRANCO", String.format("Lixo enviado ao aterro:    %d toneladas", totalLixoAterro / 1000));
        LoggerSimulacao.logRelatorio("BRANCO", "");
// Tabela de progresso de coleta por zona
        LoggerSimulacao.logRelatorio("VERDE_CLARO", "[PROGRESSO DE COLETA POR ZONA]");
        LoggerSimulacao.logRelatorio("VERDE_CLARO", "+----------+--------+----------+------------+");
        LoggerSimulacao.logRelatorio("VERDE_CLARO", "| Zona     | Gerado | Coletado | Progresso  |");
        LoggerSimulacao.logRelatorio("VERDE_CLARO", "+----------+--------+----------+------------+");
        for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
            ZonaEstatistica zona = lixoPorZona.obter(i);
            int toneladasGeradas = zona.getLixoGerado() / 1000;
            int toneladasColetadas = zona.getLixoColetado() / 1000;
            double progresso = zona.getLixoGerado() > 0 ? (double) zona.getLixoColetado() / zona.getLixoGerado() * 100 : 0.0;
            String barraProgresso = gerarBarraProgresso(progresso);
            LoggerSimulacao.logRelatorio("VERDE_CLARO", String.format("| %-8s | %6d | %8d | %5.1f%% %s |",
                    zona.getNomeZona(), toneladasGeradas, toneladasColetadas, progresso, barraProgresso));
        }
        LoggerSimulacao.logRelatorio("VERDE_CLARO", "+----------+--------+----------+------------+");
        LoggerSimulacao.logRelatorio("BRANCO", "");
        // Caminhões grandes
        LoggerSimulacao.logRelatorio("MAGENTA", "[USO DE CAMINHÕES GRANDES]");
        LoggerSimulacao.logRelatorio("MAGENTA", String.format("Número mínimo necessário: %d", totalCaminhoesGrandesUsados));
        LoggerSimulacao.logRelatorio("BRANCO", "");
        // Tempo de espera
        LoggerSimulacao.logRelatorio("AZUL_CLARO", "[DESEMPENHO NAS ESTAÇÕES]");
        double mediaEspera = descarregamentos > 0 ? (double) tempoTotalEsperaPequenos / descarregamentos : 0.0;
        LoggerSimulacao.logRelatorio("AZUL_CLARO", String.format("Tempo médio de espera na fila: %.1f min", mediaEspera));
        LoggerSimulacao.logRelatorio("BRANCO", "");
        // Caminhões Pequenos
        LoggerSimulacao.logRelatorio("AMARELO", "[STATUS DOS CAMINHÕES PEQUENOS]");
        LoggerSimulacao.logRelatorio("AMARELO", String.format("%-8s  |  %-6s  |  %-10s  |  %-15s  |  %-8s",
                "Placa", "Zona", "Carga", "Status", "Viagens"));
        var caminhoesPequenos = Simulador.getCaminhoesPequenos();
        for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
            var c = caminhoesPequenos.obter(i);
            String status = c.determinarEstado(c.getEstado());
            String corStatus = switch (c.getEstado()) {
                case 1 -> "VERDE";
                case 2, 3, 4, 5 -> "AZUL";
                case 6 -> "AZUL_CLARO";
                default -> "VERMELHO";
            };
            String linha = String.format("%-8s |  %-6s |  %2d ton /%-3d ton |  %-15s |  %-8d",
                    c.getPlaca(), c.getZonaAtual().getNome(),
                    c.getCargaAtual() / 1000, c.getCapacidade() / 1000,
                    status, c.getViagensFeitas());
            LoggerSimulacao.logRelatorio(corStatus, linha);
        }
        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Caminhões grandes
        LoggerSimulacao.logRelatorio("MAGENTA", "[STATUS DOS CAMINHÕES GRANDES]");
        LoggerSimulacao.logRelatorio("MAGENTA", String.format("%-8s  |  %-15s  |  %-10s  |  %-15s",
                "Placa", "Status", "Carga", "Estação Origem"));
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
            String linha = String.format("%-8s |  %-15s |  %3d ton /%-3dton |  %-15s",
                    c.getPlaca(), estado, c.getCargaAtual() / 1000, c.getCapacidade() / 1000,
                    c.getEstacaoOrigem() != null ? c.getEstacaoOrigem().getNome() : "-");
            LoggerSimulacao.logRelatorio(cor, linha);
        }
        LoggerSimulacao.logRelatorio("BRANCO", "");

        // Fechamento
        LoggerSimulacao.logRelatorio("CIANO_CLARO", "==================================================");
    }

    public void salvarRelatorio(String arquivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new File(arquivo), "UTF-8")) {
            String tempoFormatado = LoggerSimulacao.formatarTempo(tempoSimulado);

            // Cabeçalho
            writer.println(String.format("===== RELATÓRIO DA SIMULAÇÃO - %s =====", tempoFormatado));
            writer.println();

            // Resumo geral
            writer.println("[RESUMO GERAL]");
            double progressoTotal = totalLixoGerado > 0 ? (double) totalLixoColetado / totalLixoGerado * 100 : 0.0;
            String barraProgressoTotal = gerarBarraProgresso(progressoTotal);
            writer.println(String.format("Tempo simulado:            %d minutos", tempoSimulado));
            writer.println(String.format("Lixo total gerado:         %d toneladas", totalLixoGerado / 1000));
            writer.println(String.format("Lixo total coletado:       %d toneladas", totalLixoColetado / 1000));
            writer.println(String.format("Progresso de coleta total: %5.1f%% %s", progressoTotal, barraProgressoTotal));
            writer.println(String.format("Lixo enviado ao aterro:    %d toneladas", totalLixoAterro / 1000));
            writer.println();

            // Tabela de progresso de coleta por zona
            writer.println("[PROGRESSO DE COLETA POR ZONA]");
            writer.println("+----------+--------+----------+------------+");
            writer.println("| Zona     | Gerado | Coletado | Progresso  |");
            writer.println("+----------+--------+----------+------------+");
            for (int i = 0; i < lixoPorZona.getTamanho(); i++) {
                ZonaEstatistica zona = lixoPorZona.obter(i);
                int toneladasGeradas = zona.getLixoGerado() / 1000;
                int toneladasColetadas = zona.getLixoColetado() / 1000;
                double progresso = zona.getLixoGerado() > 0 ? (double) zona.getLixoColetado() / zona.getLixoGerado() * 100 : 0.0;
                String barraProgresso = gerarBarraProgresso(progresso);
                writer.println(String.format("| %-8s | %6d | %8d | %5.1f%% %s |",
                        zona.getNomeZona(), toneladasGeradas, toneladasColetadas, progresso, barraProgresso));
            }
            writer.println("+----------+--------+----------+------------+");
            writer.println();

            // Caminhões grandes
            writer.println("[USO DE CAMINHÕES GRANDES]");
            writer.println(String.format("Número mínimo necessário: %d", totalCaminhoesGrandesUsados));
            writer.println();

            // Tempo de espera
            writer.println("[DESEMPENHO NAS ESTAÇÕES]");
            double mediaEspera = descarregamentos > 0 ? (double) tempoTotalEsperaPequenos / descarregamentos : 0.0;
            writer.println(String.format("Tempo médio de espera na fila: %.1f min", mediaEspera));
            writer.println();

            // Caminhões pequenos
            writer.println("[STATUS DOS CAMINHÕES PEQUENOS]");
            writer.println(String.format("%-8s  |  %-6s  |  %-10s  |  %-15s  |  %-8s",
                    "Placa", "Zona", "Carga", "Status", "Viagens"));
            var caminhoesPequenos = Simulador.getCaminhoesPequenos();
            for (int i = 0; i < caminhoesPequenos.getTamanho(); i++) {
                var c = caminhoesPequenos.obter(i);
                String status = c.determinarEstado(c.getEstado());
                writer.println(String.format("%-8s |  %-6s |  %2d ton /%-3d ton |  %-15s |  %-8d",
                        c.getPlaca(), c.getZonaAtual().getNome(),
                        c.getCargaAtual() / 1000, c.getCapacidade() / 1000,
                        status, c.getViagensFeitas()));
            }
            writer.println();

            // Caminhões grandes
            writer.println("[STATUS DOS CAMINHÕES GRANDES]");
            writer.println(String.format("%-8s  |  %-15s  |  %-10s  |  %-15s",
                    "Placa", "Status", "Carga", "Estação Origem"));
            var caminhoesGrandes = Simulador.getCaminhoesGrandes();
            for (int i = 0; i < caminhoesGrandes.getTamanho(); i++) {
                var c = caminhoesGrandes.obter(i);
                String status = switch (c.getEstado()) {
                    case 0 -> "ESPERANDO";
                    case 1 -> "EM_VIAGEM_PARA_ATERRO";
                    case 2 -> "DESCARREGANDO";
                    case 3 -> "RETORNANDO";
                    default -> "DESCONHECIDO";
                };
                String estacaoOrigem = c.getEstacaoOrigem() != null ? c.getEstacaoOrigem().getNome() : "Nenhuma";
                writer.println(String.format("%-8s |  %-15s |  %2d ton /%-3d ton |  %-15s",
                        c.getPlaca(), status,
                        c.getCargaAtual() / 1000, c.getCapacidade() / 1000,
                        estacaoOrigem));
            }
            writer.println();
        }
    }

    private String gerarBarraProgresso(double progresso) {
        int blocos = (int) (progresso / 20); // Cada bloco representa ~20% de progresso
        StringBuilder barra = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            barra.append(i < blocos ? "█" : "░");
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
    public void setTempoSimulado(int tempo) {
        this.tempoSimulado = tempo;
    }
}



