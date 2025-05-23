package zonas;

import estruturas.Lista;
import simulacao.LoggerSimulacao;

public class ZonaEstatistica {
    private final String nomeZona;
    private int lixoColetado; // Total de lixo coletado na zona
    private int lixoGerado; // Total de lixo gerado na zona
    private int lixoDisponivel; // Lixo disponível para coleta no ciclo atual
    private int caminhoes; // Número de caminhões processando coleta
    private int capacidadeTotal; // Capacidade total dos caminhões na zona
    private int semColeta; // Contagem de tentativas de coleta sem sucesso
    private final Lista<Integer> coletas; // Lista de quantidades coletadas

    // Construtor da estatística de uma zona
    public ZonaEstatistica(String nomeZona) {
        this.nomeZona = nomeZona;
        this.lixoColetado = 0;
        this.lixoGerado = 0;
        this.lixoDisponivel = 0;
        this.caminhoes = 0;
        this.capacidadeTotal = 0;
        this.semColeta = 0;
        this.coletas = new Lista<>();
    }

    // Adiciona quantidade de lixo coletado e registra na lista de coletas
    public void adicionarLixo(int kg) {
        this.lixoColetado += kg;
        this.coletas.adicionar(kg);
        if (LoggerSimulacao.getModoLog() == LoggerSimulacao.ModoLog.DEBUG) {
            LoggerSimulacao.log("INFO", String.format("Zona %s: Adicionado %dkg à estatística de coleta. Total coletado: %dkg", nomeZona, kg, lixoColetado));
        }
    }

    // Registra quantidade de lixo gerado na zona
    public void adicionarLixoGerado(int kg) {
        this.lixoGerado += kg;
        if (LoggerSimulacao.getModoLog() == LoggerSimulacao.ModoLog.DEBUG) {
            LoggerSimulacao.log("INFO", String.format("Zona %s: Adicionado %dkg à estatística de geração. Total gerado: %dkg", nomeZona, kg, lixoGerado));
        }
    }

    // Inicializa estatísticas de coleta para um novo ciclo
    public void inicializarCicloColeta(int lixoDisponivel) {
        this.lixoDisponivel = lixoDisponivel;
        this.caminhoes = 0;
        this.capacidadeTotal = 0;
        this.semColeta = 0;
        this.coletas.limpar();
    }

    // Incrementa contagem de caminhões processando coleta
    public void incrementarCaminhoes() {
        this.caminhoes++;
    }

    // Adiciona capacidade de um caminhão à capacidade total
    public void adicionarCapacidade(int capacidade) {
        this.capacidadeTotal += capacidade;
    }

    // Registra uma tentativa de coleta sem sucesso
    public void registrarSemColeta() {
        this.semColeta++;
    }

    // Getters
    public String getNomeZona() { return nomeZona; }
    public int getLixoColetado() { return lixoColetado; }
    public int getLixoGerado() { return lixoGerado; }
    public int getLixoDisponivel() { return lixoDisponivel; }
    public int getCaminhoes() { return caminhoes; }
    public int getCapacidadeTotal() { return capacidadeTotal; }
    public int getSemColeta() { return semColeta; }
    public Lista<Integer> getColetas() { return coletas; }
}


