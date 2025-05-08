package caminhoes;

import zonas.ZonaUrbana;

public class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    protected static final int[] OPCOES = {2000, 4000, 8000, 10000};
    private static String id;
    protected int limiteViagens;
    protected int viagensFeitas;
    protected int status;
    private ZonaUrbana zonaAtual;
    private ZonaUrbana zonaBase;

    public CaminhaoPequeno(int escolha, int maxViagens, ZonaUrbana zonaBase, String placaOpcional) {
        this.cargaAtual = 0;
        this.capacidade = determinarCapacidade(escolha);
        CaminhaoPequeno.id = processarPlaca(placaOpcional);
        this.status = 0;
    }

    public CaminhaoPequeno(int escolha, int maxViagens, ZonaUrbana zonaBase) {
        this(escolha, maxViagens,zonaBase  ,null);
    }

    private int determinarCapacidade(int escolha) {
        if (escolha < 1 || escolha > 4) {
            throw new IllegalArgumentException("Escolha deve ser de 1 a 4.");
        }
        return OPCOES[escolha - 1];
    }

    public int getEstado() {
        return status;
    }

    public void setEstado(int status) {
        this.status = status;
    }

    public String determinarEstado(int status) {
        return switch (status) {
            case 1 -> "DISPONÍVEL";
            case 2 -> "COLETANDO";
            case 3 -> "INDO_ESTAÇÃO";
            case 4 -> "FILA_ESTAÇÃO";
            case 5 -> "DESCARREGANDO";
            case 6 -> "ENCERRADO";
            default -> "DESCONHECIDO";
        };
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

    public String getPlaca() {
        return id;
    }

    public boolean limiteAtingido(int viagensFeitas){
        return viagensFeitas < limiteViagens;
    }


    public void setZonaAtual(ZonaUrbana zona) {
        this.zonaAtual = zona;
    }

    public ZonaUrbana getZonaAtual() {
        return zonaAtual;
    }

    public ZonaUrbana getZonaBase() {
        return zonaBase;
    }

    public boolean coletar(int quantidade) {
        if (cargaAtual + quantidade <= capacidade) {
            cargaAtual += quantidade;
            return true;
        }
        return false;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getViagensFeitas() {
        return viagensFeitas;
    }

    public boolean estaCheio() {
        return cargaAtual >= capacidade;
    }

    public int descarregar() {
        int carga = cargaAtual;
        cargaAtual = 0;
        return carga;
    }

    public int getCargaAtual() {
        return cargaAtual;
    }
}