package zonas;

import java.util.Random;

public class ZonaUrbana {
    private final String nome;
    private int lixoAcumulado;
    private final int lixoMin;
    private final int lixoMax;
    private static final String[] ZONAS = {"Norte", "Sul", "Leste", "Sudeste", "Centro"};
    //Variação no tempo de trânsito por zona. Ex: O Centro é mais estreito e mais transitado, logo o tempo de trânsito dele
    //é maior do que o de outras zonas, a variação é baseada na zona Norte, portanto ela tem 0 0 de variação
    //Essa variação irá adicionar ou remover tempos de trânsito dependendo da zona
    private static final int[] VARIACOES_PICO =    {0, 10, -7, 5, 10};
    private static final int[] VARIACOES_NORMAL =  {0, 5, -5, 3, -10};
    private static final int[][] distanciasZonas = {
            // Sul, Norte, Centro, Leste, Sudeste
            { 0, 8, 3, 5, 4 },  // Sul
            { 8, 0, 5, 4, 6 },  // Norte
            { 3, 5, 0, 2, 3 },  // Centro
            { 5, 4, 2, 0, 3 },  // Leste
            { 4, 6, 3, 3, 0 }   // Sudeste
    };
    private int caminhoesAtivos = 0;
    private static int VARIACAO_TRANSITO_PICO = 0;
    private static int VARIACAO_TRANSITO_NORMAL = 0;

    public ZonaUrbana(int escolha, int lixoMin, int lixoMax) {
        this.nome = determinarZona(escolha);
        VARIACAO_TRANSITO_PICO = VARIACOES_PICO[escolha - 1];
        VARIACAO_TRANSITO_NORMAL = VARIACOES_NORMAL[escolha - 1];
        this.lixoAcumulado = 0;
        this.lixoMin = lixoMin;
        this.lixoMax = lixoMax;
    }

    private String determinarZona(int escolha) {
        if (escolha < 1 || escolha > 5) {
            throw new IllegalArgumentException("Escolha deve ser de 1 a 5.");
        }
        return ZONAS[escolha - 1];
    }

    private static int getIndiceZona(String nome) {
        for (int i = 0; i < ZONAS.length; i++) {
            if (ZONAS[i].equalsIgnoreCase(nome)) return i;
        }
        return -1;
    }

    public static int getDistancia(String zonaA, String zonaB) {
        int i = getIndiceZona(zonaA);
        int j = getIndiceZona(zonaB);
        if (i == -1 || j == -1) return Integer.MAX_VALUE;
        return distanciasZonas[i][j];
    }

    public void gerarLixo() {
        int quantidade = new Random().nextInt(lixoMin, lixoMax);
        System.out.println("Zona " + nome + ": Gerou " + quantidade + "kg de lixo. Total: " + lixoAcumulado + "kg.");
    }
    
    public int coletarLixo(int quantidade) {
        int coletado = Math.min(quantidade, lixoAcumulado);
        lixoAcumulado -= coletado;
        return coletado;
    }

    public void incrementarCaminhoesAtivos() {
        caminhoesAtivos++;
    }

    public void decrementarCaminhoesAtivos() {
        if (caminhoesAtivos > 0) caminhoesAtivos--;
    }

    public int getLixoAcumulado() {
        return lixoAcumulado;
    }

    public String getNome() {
        return nome;
    }

    public int getVariacaoPico() {
        return VARIACAO_TRANSITO_PICO;
    }

    public int getVariacaoNormal() {
        return VARIACAO_TRANSITO_NORMAL;
    }

    public int getCaminhoesAtivos() {
        return caminhoesAtivos;
    }
}