package zonas;

import estruturas.Lista;

public class ZonaEstatistica {
    private final String nomeZona;
    private int lixoColetado;
    private int lixoGerado;
    // Campos para rastrear coletas temporárias
    private int caminhoes;
    private int capacidadeTotal;
    private int semColeta;
    private Lista<Integer> coletas;

    // Construtor da estatística de uma zona
    public ZonaEstatistica(String nomeZona) {
        this.nomeZona = nomeZona;
        this.lixoColetado = 0;
        this.lixoGerado = 0;
        this.caminhoes = 0;
        this.capacidadeTotal = 0;
        this.semColeta = 0;
        this.coletas = new Lista<>();
    }

    public void adicionarLixo(int kg) {
        this.lixoColetado += kg;
    }

    public void adicionarLixoGerado(int kg) {
        this.lixoGerado += kg;
    }

    public void incrementarCaminhoes(int capacidade) {
        this.caminhoes++;
        this.capacidadeTotal += capacidade;
    }

    public void registrarTentativaSemColeta() {
        this.semColeta++;
    }

    public void adicionarColeta(int kg) {
        this.coletas.adicionar(kg);
    }

    public void resetarEstatisticasTemporarias() {
        this.caminhoes = 0;
        this.capacidadeTotal = 0;
        this.semColeta = 0;
        this.coletas.limpar();
    }

    // Getters
    public String getNomeZona() { return nomeZona; }
    public int getLixoColetado() { return lixoColetado; }
    public int getLixoGerado() { return lixoGerado; }
    public int getCaminhoes() { return caminhoes; }
    public int getCapacidadeTotal() { return capacidadeTotal; }
    public int getSemColeta() { return semColeta; }
    public Lista<Integer> getColetas() { return coletas; }
}


