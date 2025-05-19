package simulacao;

import estruturas.MapaEventos;

// Gerencia logs da simulação com modos NORMAL e DEBUG, usando cores ANSI
public class LoggerSimulacao {
    // Modos de log
    public enum ModoLog { NORMAL, DEBUG }
    private static ModoLog modoAtual = ModoLog.NORMAL;

    // Códigos ANSI para cores
    private static final String RESET = "\u001B[0m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARELO = "\u001B[33m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String CIANO = "\u001B[36m";
    private static final String AZUL = "\u001B[34m";

    // Mapeia tipos de evento a cores usando MapaEventos
    private static final MapaEventos CORES_EVENTO = new MapaEventos();
    static {
        CORES_EVENTO.put("COLETA", VERDE);
        CORES_EVENTO.put("CHEGADA", CIANO);
        CORES_EVENTO.put("DESCARGA", AZUL);
        CORES_EVENTO.put("ERRO", VERMELHO);
        CORES_EVENTO.put("INFO", AMARELO);
    }
    // Define o modo de log
    public static void setModoLog(ModoLog modo) {
        modoAtual = modo;
        log("INFO", "Modo de log alterado para " + modo);
    }

    public static ModoLog getModoLog(){
        return modoAtual;
    }

    // Registra uma mensagem de log com tipo de evento e timestamp
    public static void log(String tipoEvento, String mensagem) {
        synchronized (System.out) {
            String cor = CORES_EVENTO.get(tipoEvento);
            if (cor == null) {
                cor = RESET; // Cor padrão se o evento não for encontrado
            }
            String timestamp = formatarTempo(Simulador.getTempoSimulado());
            String mensagemFormatada = String.format("%s[%s] %s%s", cor, timestamp, mensagem, RESET);
            // No modo NORMAL, filtra mensagens menos relevantes
            if (modoAtual == ModoLog.NORMAL) {
                if (mensagem.contains("não disponível") || mensagem.contains("sem lixo disponível") ||
                        mensagem.contains("já atribuído")) {
                    return; // Ignora mensagens redundantes
                }
                System.out.println(mensagemFormatada);
            } else {
                // No modo DEBUG, imprime tudo com detalhes adicionais
                System.out.println(mensagemFormatada);
            }
        }
    }
    // Formata o tempo simulado em "Dia X, HH:MM"
    private static String formatarTempo(int minutos) {
        int dias = minutos / (24 * 60);
        int horas = (minutos % (24 * 60)) / 60;
        int mins = minutos % 60;
        return String.format("Dia %d, %02d:%02d", dias + 1, horas, mins);
    }
}

