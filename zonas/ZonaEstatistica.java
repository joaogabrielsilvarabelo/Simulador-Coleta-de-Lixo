package zonas;

public class ZonaEstatistica {
    private String nomeZona;
    private int lixoColetado;

    public ZonaEstatistica(String nomeZona) {
        this.nomeZona = nomeZona;
        this.lixoColetado = 0;
    }

    public String getNomeZona() {
        return nomeZona;
    }

    public int getLixoColetado() {
        return lixoColetado;
    }

    public void adicionarLixo(int kg) {
        this.lixoColetado += kg;
    }
}
