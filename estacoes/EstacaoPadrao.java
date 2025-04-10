package estacoes;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;

public class EstacaoPadrao extends EstacaoTransferencia {

    private int lixoArmazenado;

    public EstacaoPadrao(String nome) {
        super(nome);
        this.lixoArmazenado = 0;
    }

    @Override
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        int descarregado = caminhao.descarregar();
        lixoArmazenado += descarregado;
        System.out.println("Estação " + nome + " recebeu " + descarregado + "kg de lixo.");
    }

    @Override
    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        caminhao.carregar(lixoArmazenado);
        System.out.println("Estação " + nome + " carregou caminhão grande com " + lixoArmazenado + "kg.");
        lixoArmazenado = 0;
    }
}