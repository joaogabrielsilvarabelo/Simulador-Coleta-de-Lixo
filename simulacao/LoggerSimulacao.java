package simulacao;

import caminhoes.DistribuicaoCaminhoes;
import estruturas.MapaEventos;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

// Classe para gerenciar logs do sistema com formatação colorida e saída para arquivo
public class LoggerSimulacao {
    public enum ModoLog { NORMAL, DEBUG }
    private static ModoLog modoAtual = ModoLog.NORMAL;

    private static PrintWriter escritorArquivoLog;
    private static String nomeArquivoLog;

    // Cores ANSI para formatação no terminal
    public static final String RESET = "\u001B[0m";
    public static final String VERMELHO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARELO = "\u001B[33m";
    public static final String AZUL = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CIANO = "\u001B[36m";
    public static final String BRANCO = "\u001B[37m";
    public static final String CIANO_CLARO = "\u001B[96m";
    public static final String CINZA = "\u001B[90m";
    protected static final String AMARELO_CLARO = "\u001B[93m";
    public static final String VERDE_CLARO = "\u001B[92m";
    public static final String MAGENTA_CLARO = "\u001B[95m";
    public static final String AZUL_CLARO = "\u001B[94m";

    private static final MapaEventos CORES_EVENTO = new MapaEventos();
    static {
        CORES_EVENTO.put("COLETA", VERDE);
        CORES_EVENTO.put("CHEGADA", AZUL_CLARO);
        CORES_EVENTO.put("DESCARGA", CIANO_CLARO);
        CORES_EVENTO.put("ERRO", VERMELHO);
        CORES_EVENTO.put("INFO", AMARELO_CLARO);
        CORES_EVENTO.put("CONFIG", RESET);
        CORES_EVENTO.put("ESTATISTICA", RESET);
        CORES_EVENTO.put("VIAGEM", AZUL);
        CORES_EVENTO.put("ATRIBUICAO", MAGENTA_CLARO);
        CORES_EVENTO.put("ADIÇÃO", VERDE_CLARO);
    }

    private static final MapaEventos CORES_RELATORIO = new MapaEventos();
    static {
        CORES_RELATORIO.put("VERDE", VERDE);
        CORES_RELATORIO.put("VERDE_CLARO", VERDE_CLARO);
        CORES_RELATORIO.put("CIANO", CIANO);
        CORES_RELATORIO.put("AZUL", AZUL);
        CORES_RELATORIO.put("AZUL_CLARO", AZUL_CLARO);
        CORES_RELATORIO.put("VERMELHO", VERMELHO);
        CORES_RELATORIO.put("AMARELO", AMARELO_CLARO);
        CORES_RELATORIO.put("BRANCO", RESET);
        CORES_RELATORIO.put("MAGENTA", MAGENTA_CLARO);
        CORES_RELATORIO.put("CINZA", CINZA);
        CORES_RELATORIO.put("CIANO_CLARO", CIANO_CLARO);
    }

    public static void inicializarLogArquivo(String fileName) {
        nomeArquivoLog = fileName;
        try {
            escritorArquivoLog = new PrintWriter(new File(fileName), "UTF-8");
            log("CONFIG", String.format("Log de eventos será salvo em %s", fileName));
        } catch (IOException e) {
            log("ERRO", String.format("Erro ao inicializar arquivo de log %s: %s", fileName, e.getMessage()));
        }
    }

    // Close the log file
    public static void fecharLogArquivo() {
        if (escritorArquivoLog != null) {
            escritorArquivoLog.close();
            escritorArquivoLog = null;
            log("CONFIG", String.format("Arquivo de log %s fechado.", nomeArquivoLog));
        }
    }

    public static void setModoLog(ModoLog modo) {
        modoAtual = modo;
        log("CONFIG", String.format("Modo de log alterado para %s.", modo));
    }

    public static ModoLog getModoLog() {
        return modoAtual;
    }

    // Registro especial para o relatório horário
    public static void logRelatorio(String tipoEvento, String mensagem) {
        synchronized (System.out) {
            String cor = CORES_RELATORIO.get(tipoEvento);
            if (cor == null) {
                cor = RESET;
            }
            String mensagemFormatada = String.format("%s %s%s", cor, mensagem, RESET);
            System.out.println(mensagemFormatada);
        }
        if (escritorArquivoLog != null) {
            synchronized (escritorArquivoLog) {
                // Tira todas as cores pro arquivo
                String mensagemSemCores = mensagem.replaceAll("\u001B\\[[0-9;]*m", "");
                escritorArquivoLog.println(mensagemSemCores);
                escritorArquivoLog.flush();
            }
        }
    }

    // Registra uma mensagem de log com tipo de evento e timestamp
    public static void log(String tipoEvento, String mensagem) {
        synchronized (System.out) {
            String cor = CORES_EVENTO.get(tipoEvento);
            if (cor == null) {
                cor = RESET;
            }
            String timestamp = formatarTempo(Simulador.getTempoSimulado());
            String mensagemFormatada = String.format("%s[%s] %s%s", cor, timestamp, mensagem, RESET);
            String mensagemArquivo = String.format("[%s] %s", timestamp, mensagem);
            if (Objects.equals(tipoEvento, "CONFIG") || Objects.equals(tipoEvento, "ESTATISTICA")){
                mensagemFormatada = String.format("%s %s%s", cor, mensagem, RESET);
                mensagemArquivo = mensagem;
            }
            if (modoAtual == ModoLog.NORMAL) {
                if (mensagem.contains("não disponível") && !tipoEvento.equals("ERRO")) {
                    return;
                }
                System.out.println(mensagemFormatada);
            } else {
                System.out.println(mensagemFormatada);
            }
            if (escritorArquivoLog != null) {
                synchronized (escritorArquivoLog) {
                    escritorArquivoLog.println(String.format("[%s] %s", tipoEvento, mensagemArquivo));
                    escritorArquivoLog.flush();
                }
            }
        }
    }

    static String formatarTempo(int minutos) {
        int dias = minutos / (24 * 60);
        int horas = (minutos % (24 * 60)) / 60;
        int mins = minutos % 60;
        if(DistribuicaoCaminhoes.isHorarioDePico(minutos)){
            return String.format("Dia %d, %02d:%02d [PICO]", dias + 1, horas, mins);
        }
        return String.format("Dia %d, %02d:%02d", dias + 1, horas, mins);
    }
}


