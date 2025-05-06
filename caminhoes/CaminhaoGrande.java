package caminhoes;

public class CaminhaoGrande {
    protected int capacidadeMaxima = 20000;
    protected int cargaAtual;
    private static String id;

    public CaminhaoGrande(String placaOpcional) {
        this.cargaAtual = 0;
        CaminhaoGrande.id = processarPlaca(placaOpcional);
    }

    public CaminhaoGrande() {
        this(null);
    }

    private static String processarPlaca(String placaOpcional) {
        if (placaOpcional != null) {
            if (!Placa.validarPlaca(placaOpcional)) {
                throw new IllegalArgumentException("Placa não segue normas do Mercosul");
            }
            if (!placaOpcional.isBlank()) {
                return placaOpcional.toUpperCase();
            }
        }
        return Placa.gerarPlaca();
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