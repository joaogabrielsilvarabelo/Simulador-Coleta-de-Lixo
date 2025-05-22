package zonas;

import simulacao.LoggerSimulacao;

import java.util.Random;

public class ZonaUrbana {
    private static final String[] ZONAS = {"Norte", "Sul", "Leste", "Sudeste", "Centro"};
    private static final int[] VARIACOES_PICO = {0, 10, 7, 5, 10};
    private static final int[] VARIACOES_NORMAL = {0, 5, -5, 3, -7};
    //Variação no tempo de trânsito por zona. Ex: O Centro é mais estreito e mais transitado, logo o tempo de trânsito dele
    //é maior do que o de outras zonas, a variação é baseada na zona Norte, portanto ela tem 0 0 de variação
    //Essa variação irá adicionar ou remover tempos de trânsito dependendo da zona
    private static final int[][] DISTANCIAS_ZONAS = {
            //  Sul  N  C   L   Sud
            {0, 13, 14, 11, 11},  // Sul
            {13, 0, 11, 9, 16},   // Norte
            {14, 11, 0, 9, 16},   // Centro
            {11, 9,  9, 0, 13},    // Leste
            {11, 16, 16, 13, 0}   // Sudeste
    };
    private final String nome;
    private int lixoAcumulado;
    private final int lixoMin;
    private final int lixoMax;
    private final int variacaoPico;
    private final int variacaoNormal;
    private int caminhoesAtivos;

    public ZonaUrbana(int escolha, int lixoMin, int lixoMax) {
        this.nome = determinarZona(escolha);
        this.lixoMin = lixoMin;
        this.lixoMax = lixoMax;
        this.variacaoPico = VARIACOES_PICO[escolha];
        this.variacaoNormal = VARIACOES_NORMAL[escolha];
        this.lixoAcumulado = 0;
        this.caminhoesAtivos = 0;
    }

    private String determinarZona(int escolha) {
        if (escolha < 0 || escolha > 4) {
            throw new IllegalArgumentException("Escolha deve ser de 0 a 4.");
        }
        return ZONAS[escolha];
    }

    public static int getDistancia(String zonaA, String zonaB) {
        int i = getIndiceZona(zonaA);
        int j = getIndiceZona(zonaB);
        if (i == -1 || j == -1) {
            return Integer.MAX_VALUE;
        }
        return DISTANCIAS_ZONAS[i][j];
    }

    private static int getIndiceZona(String nome) {
        for (int i = 0; i < ZONAS.length; i++) {
            if (ZONAS[i].equalsIgnoreCase(nome)) {
                return i;
            }
        }
        return -1;
    }

    public int gerarLixo() {
        int quantidade = new Random().nextInt(lixoMin, lixoMax + 1);
        lixoAcumulado += quantidade;
        LoggerSimulacao.log("CONFIG", String.format("Zona %s: Gerou %dkg de lixo. Total: %dkg.", nome, quantidade, lixoAcumulado));
        return quantidade;
    }

    public int coletarLixo(int quantidade) {
        int coletado = Math.min(quantidade, lixoAcumulado);
        lixoAcumulado -= coletado;
        if (LoggerSimulacao.getModoLog() == LoggerSimulacao.ModoLog.DEBUG) {
            LoggerSimulacao.log("COLETA", String.format("Zona %s: Coletado %dkg de lixo. Lixo restante: %dkg.", nome, coletado, lixoAcumulado));
        }
        return coletado;
    }

    public void incrementarCaminhoesAtivos() {
        caminhoesAtivos++;
    }

    public void decrementarCaminhoesAtivos() {
        if (caminhoesAtivos > 0) {
            caminhoesAtivos--;
        }
    }

    public int getLixoAcumulado() {return lixoAcumulado;}
    public String getNome() {return nome;}
    public int getVariacaoPico() { return variacaoPico; }
    public int getVariacaoNormal() { return variacaoNormal; }
    public int getCaminhoesAtivos() { return caminhoesAtivos; }
}


