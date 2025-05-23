package estacoes;

import caminhoes.CaminhaoPequeno;

public class ResultadoProcessamentoFila {
    private final CaminhaoPequeno caminhaoProcessado;
    private final int tempoDeEspera;
    private final boolean foiProcessado;

    public ResultadoProcessamentoFila(CaminhaoPequeno caminhao, int tempoDeEspera, boolean foiProcessado) {
        this.caminhaoProcessado = caminhao;
        this.tempoDeEspera = tempoDeEspera;
        this.foiProcessado = foiProcessado;
    }

    public CaminhaoPequeno getCaminhaoProcessado() { return caminhaoProcessado; }
    public int getTempoDeEspera() { return tempoDeEspera; }
    public boolean foiProcessado() { return foiProcessado; }
}
