package caminhoes;

public class CaminhaoPequenoPadrao extends CaminhaoPequeno {

    public CaminhaoPequenoPadrao() {
        this.capacidade = 2000;
        this.cargaAtual = 0;
    }

    @Override
    public boolean coletar(int quantidade) {
        if (cargaAtual + quantidade <= capacidade) {
            cargaAtual += quantidade;
            return true;
        }
        return false;
    }
}