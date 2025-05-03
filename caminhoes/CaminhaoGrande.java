package caminhoes;

public class CaminhaoGrande {
    protected int capacidadeMaxima = 20000;
    protected int cargaAtual;
    private static String id;

    public CaminhaoGrande(String placaOpcional) {
        this.cargaAtual = 0;
        if (placaOpcional != null && !placaOpcional.isBlank() && Placa.validarPlaca(placaOpcional)) {
            CaminhaoGrande.id = placaOpcional.toUpperCase();
        } else {
            CaminhaoGrande.id = Placa.gerarPlaca();
        }
    }

    public CaminhaoGrande() {
        this(null);
    }

    public void carregar(int quantidade) {
        cargaAtual += quantidade;
        if (cargaAtual > capacidadeMaxima) {
            cargaAtual = capacidadeMaxima;
        }
    }

    public boolean prontoParaPartir() {
        return cargaAtual >= capacidadeMaxima;
    }

    public void descarregar() {
        System.out.println("Caminhão grande partiu para o aterro com " + cargaAtual + "kg.");
        cargaAtual = 0;
    }

    public String getPlaca() {
        return id;
    }


}