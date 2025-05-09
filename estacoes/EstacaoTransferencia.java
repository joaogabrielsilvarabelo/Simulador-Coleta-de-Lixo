package estacoes;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import estruturas.Fila;
import estruturas.Lista;

public class EstacaoTransferencia {
    private String nome;
    private int lixoArmazenado;
    private Fila<CaminhaoPequeno> filaPequenos;
    private Lista<CaminhaoGrande> listaGrandes;
    private int esperaMaxPequenos;
    private int esperaTotalPequenos;
    private CaminhaoGrande caminhaoGrandeEsperando;
    private boolean TEM_CAMINHAO_GRANDE_ESPERANDO;

    public EstacaoTransferencia(String nome, int esperaMaxPequenos) {
        this.nome = nome;
        this.lixoArmazenado = 0;
        this.filaPequenos = new Fila<>();
        this.listaGrandes = new Lista<>();
        this.esperaMaxPequenos = esperaMaxPequenos;
        this.esperaTotalPequenos = 0;
        this.caminhaoGrandeEsperando = null;
        this.TEM_CAMINHAO_GRANDE_ESPERANDO = false;
    }

    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao, int tempoAtual) {
        filaPequenos.enfileirar(caminhao);
        if (filaPequenos.getTamanho() == 1) {
            this.esperaTotalPequenos = 0;
        }
    }

    public ResultadoProcessamentoFila processarFila(int tempoAtual) {
        if (filaPequenos.estaVazia()) {
            esperaTotalPequenos = 0;
            return new ResultadoProcessamentoFila(null, 0, false);
        }
        esperaTotalPequenos++;
        if (!TEM_CAMINHAO_GRANDE_ESPERANDO || caminhaoGrandeEsperando == null) {
            return new ResultadoProcessamentoFila(null, esperaTotalPequenos, false);
        }
        CaminhaoPequeno caminhaoPequeno = filaPequenos.primeiroDaFila();
        if (caminhaoGrandeEsperando.getCargaAtual() + caminhaoPequeno.getCargaAtual() <= caminhaoGrandeEsperando.getCapacidade()) {
            caminhaoPequeno = filaPequenos.remover();
            int cargaDescarregada = caminhaoPequeno.descarregar();
            caminhaoGrandeEsperando.carregar(cargaDescarregada);
            System.out.printf("Estação %s: Caminhão %s descarregou %dkg. Carga Atual: %d/%d kg. Caminhões restantes: %d.%n",
                    nome, caminhaoPequeno.getPlaca(), cargaDescarregada,
                    caminhaoGrandeEsperando.getCargaAtual(), caminhaoGrandeEsperando.getCapacidade(),
                    filaPequenos.getTamanho());
            int tempoEspera = esperaTotalPequenos;
            esperaTotalPequenos = 0; // Reset wait time after processing
            return new ResultadoProcessamentoFila(caminhaoPequeno, tempoEspera, true);
        }
        return new ResultadoProcessamentoFila(null, esperaTotalPequenos, false);
    }

    public boolean temCaminhaoGrandeFila() {
        if (caminhaoGrandeEsperando == null) {
            return TEM_CAMINHAO_GRANDE_ESPERANDO = false;
        }
        return TEM_CAMINHAO_GRANDE_ESPERANDO = true;
    }

    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        caminhao.carregar(lixoArmazenado);
        System.out.println("Estação " + nome + " carregou caminhão grande com " + lixoArmazenado + "kg.");
        lixoArmazenado = 0;
    }

    public void adicionarCaminhaoGrande(CaminhaoGrande grande) {
        listaGrandes.adicionar(grande);
    }

    // Added methods to fix errors
    public Fila<CaminhaoPequeno> getFilaPequenos() {
        return filaPequenos;
    }

    public boolean tempoEsperaExcedido() {
        return esperaTotalPequenos > esperaMaxPequenos;
    }

    public String getNome() {
        return nome;
    }

    public boolean precisaCaminhaoGrande() {
        return filaPequenos.getTamanho() > 0 && caminhaoGrandeEsperando == null;
    }

    public void atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        this.caminhaoGrandeEsperando = caminhao;
        this.TEM_CAMINHAO_GRANDE_ESPERANDO = true;
    }
}