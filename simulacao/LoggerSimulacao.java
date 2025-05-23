package simulacao;

import estruturas.MapaEventos;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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

    private static final MapaEventos CORES_EVENTO = new MapaEventos();
    static {
        CORES_EVENTO.put("COLETA", VERDE);
        CORES_EVENTO.put("CHEGADA", CIANO);
        CORES_EVENTO.put("DESCARGA", CIANO_CLARO);
        CORES_EVENTO.put("ERRO", VERMELHO);
        CORES_EVENTO.put("INFO", AMARELO_CLARO);
        CORES_EVENTO.put("CONFIG", RESET);
        CORES_EVENTO.put("ESTATISTICA", RESET);
        CORES_EVENTO.put("VIAGEM", AZUL);
        CORES_EVENTO.put("ATRIBUICAO", MAGENTA);
    }

    private static final MapaEventos CORES_RELATORIO = new MapaEventos();
    static {
        CORES_RELATORIO.put("VERDE", VERDE);
        CORES_RELATORIO.put("CIANO", CIANO);
        CORES_RELATORIO.put("AZUL", AZUL);
        CORES_RELATORIO.put("VERMELHO", VERMELHO);
        CORES_RELATORIO.put("AMARELO", AMARELO_CLARO);
        CORES_RELATORIO.put("BRANCO", RESET);
        CORES_RELATORIO.put("MAGENTA", MAGENTA);
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
    public static void logRelatorio(String cor, String mensagem) {
        synchronized (System.out) {
            String cor_lista = CORES_RELATORIO.get(cor);
            if (cor_lista == null) {
                cor_lista = RESET;
            }
            String mensagemFormatada = String.format("%s %s%s", cor_lista, mensagem, RESET);
            System.out.println(mensagemFormatada);
        }
        if (escritorArquivoLog != null) {
            synchronized (escritorArquivoLog) {
                escritorArquivoLog.println(mensagem);
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
            if (tipoEvento == "CONFIG" || tipoEvento == "ESTATISTICA"){
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
        return String.format("Dia %d, %02d:%02d", dias + 1, horas, mins);
    }
}


