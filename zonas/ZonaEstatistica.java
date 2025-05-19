
package zonas;

public class ZonaEstatistica {
    private final String nomeZona;
    private int lixoColetado;
    private int lixoGerado;

    // Construtor da estatística de uma zona
    public ZonaEstatistica(String nomeZona) {
        this.nomeZona = nomeZona;
        this.lixoColetado = 0;
        this.lixoGerado = 0;
    }

    public String getNomeZona() {
        return nomeZona;
    }

    public int getLixoColetado() {
        return lixoColetado;
    }

    public int getLixoGerado() {
        return lixoGerado;
    }

    public void adicionarLixo(int kg) {
        this.lixoColetado += kg;
    }

    public void adicionarLixoGerado(int kg) {
        this.lixoGerado += kg;
    }
}

