package estacoes;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import estruturas.Fila;

public class EstacaoTransferencia {

    private String nome;
    private int lixoArmazenado;
    private Fila filaCaminhoes;

    public EstacaoTransferencia(String nome) {
        this.nome = nome;
        this.lixoArmazenado = 0;
        this.filaCaminhoes = new Fila();
    }

    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        filaCaminhoes.enfileirar(caminhao.getPlaca());
        int descarregado = caminhao.descarregar();
        lixoArmazenado += descarregado;
        System.out.println("Estação " + nome + " recebeu " + descarregado +
                "kg de lixo do caminhão " + caminhao.getPlaca());
    }

    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        caminhao.carregar(lixoArmazenado);
        System.out.println("Estação " + nome + " carregou caminhão grande com " + lixoArmazenado + "kg.");
        lixoArmazenado = 0;
    }
}