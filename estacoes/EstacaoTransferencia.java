package estacoes;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import estruturas.Fila;
import estruturas.Lista;

public class EstacaoTransferencia {

    private String nome;
    private int lixoArmazenado;
    private Fila filaPequenos;
    private Lista listaGrandes;

    public EstacaoTransferencia(String nome) {
        this.nome = nome;
        this.lixoArmazenado = 0;
        this.filaPequenos = new Fila();
    }

    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        filaPequenos.enfileirar(caminhao.getPlaca());
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


    public void adicionarCaminhaoGrande(CaminhaoGrande grande) {
        listaGrandes.adicionar(grande);
    }

}