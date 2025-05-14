package estacoes;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import estruturas.Fila;
import estruturas.Lista;
import zonas.ZonaUrbana;

public class EstacaoTransferencia {
    private final String nome;
    private int lixoArmazenado;
    private final Fila<CaminhaoPequeno> filaPequenos;
    private final Lista<CaminhaoGrande> listaGrandes;
    private final int esperaMaxPequenos;
    private int esperaTotalPequenos;
    private CaminhaoGrande caminhaoGrandeEsperando;
    private ZonaUrbana zonaDaEstacao;
    private boolean temCaminhaoGrandeEsperando;
    private int tempoEsperaCaminhaoGrande;

    public EstacaoTransferencia(String nome, int esperaMaxPequenos, ZonaUrbana zonaDaEstacao) {
        this.nome = nome;
        this.lixoArmazenado = 0;
        this.filaPequenos = new Fila<>();
        this.listaGrandes = new Lista<>();
        this.esperaMaxPequenos = esperaMaxPequenos;
        this.esperaTotalPequenos = 0;
        this.caminhaoGrandeEsperando = null;
        this.zonaDaEstacao = zonaDaEstacao;
        this.temCaminhaoGrandeEsperando = false;
        this.tempoEsperaCaminhaoGrande = 0;
    }

    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao, int tempoAtual) {
        filaPequenos.enfileirar(caminhao);
        esperaTotalPequenos += filaPequenos.getTamanho(); // Accumulate wait time for all trucks in queue
    }

    public ResultadoProcessamentoFila processarFila(int tempoAtual) {
        if (filaPequenos.estaVazia()) {
            esperaTotalPequenos = 0;
            return new ResultadoProcessamentoFila(null, 0, false);
        }
        if (!temCaminhaoGrandeEsperando || caminhaoGrandeEsperando == null) {
            esperaTotalPequenos += filaPequenos.getTamanho();
            return new ResultadoProcessamentoFila(null, esperaTotalPequenos, false);
        }
        CaminhaoPequeno caminhaoPequeno = filaPequenos.primeiroDaFila();
        if (caminhaoGrandeEsperando.getCargaAtual() + caminhaoPequeno.getCargaAtual() <= caminhaoGrandeEsperando.getCapacidade()) {
            caminhaoPequeno = filaPequenos.remover();
            int cargaDescarregada = caminhaoPequeno.descarregar();
            caminhaoGrandeEsperando.carregar(cargaDescarregada);
            logProcessamento(caminhaoPequeno, cargaDescarregada);
            int tempoEspera = filaPequenos.estaVazia() ? esperaTotalPequenos : esperaTotalPequenos / filaPequenos.getTamanho();
            esperaTotalPequenos = 0;
            return new ResultadoProcessamentoFila(caminhaoPequeno, tempoEspera, true);
        }
        esperaTotalPequenos += filaPequenos.getTamanho();
        return new ResultadoProcessamentoFila(null, esperaTotalPequenos, false);
    }

    private void logProcessamento(CaminhaoPequeno caminhao, int cargaDescarregada) {
        System.out.printf("%s: Caminhão %s descarregou %dkg. Carga Atual: %d/%d kg. Caminhões restantes: %d.%n",
                nome, caminhao.getPlaca(), cargaDescarregada,
                caminhaoGrandeEsperando.getCargaAtual(), caminhaoGrandeEsperando.getCapacidade(),
                filaPequenos.getTamanho());
    }

    public void atualizarTempoEsperaCaminhaoGrande() {
        if (temCaminhaoGrandeEsperando && caminhaoGrandeEsperando != null) {
            tempoEsperaCaminhaoGrande++;
        }
    }

    public boolean toleranciaCaminhaoGrandeExcedida() {
        return temCaminhaoGrandeEsperando && tempoEsperaCaminhaoGrande > caminhaoGrandeEsperando.getTempoTolerancia();
    }

    public CaminhaoGrande liberarCaminhaoGrandeSeNecessario() {
        if (toleranciaCaminhaoGrandeExcedida() && caminhaoGrandeEsperando.getCargaAtual() > 0) {
            CaminhaoGrande cg = caminhaoGrandeEsperando;
            caminhaoGrandeEsperando = null;
            temCaminhaoGrandeEsperando = false;
            tempoEsperaCaminhaoGrande = 0;
            cg.descarregar();
            return cg;
        }
        return null;
    }

    public boolean temCaminhaoGrandeFila() {
        temCaminhaoGrandeEsperando = caminhaoGrandeEsperando != null;
        return temCaminhaoGrandeEsperando;
    }

    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        caminhao.carregar(lixoArmazenado);
        System.out.println("Estação " + nome + " carregou caminhão grande com " + lixoArmazenado + "kg.");
        lixoArmazenado = 0;
    }

    public Fila<CaminhaoPequeno> getFilaPequenos() {
        return filaPequenos;
    }

    public boolean tempoEsperaExcedido() {
        return esperaTotalPequenos > esperaMaxPequenos;
    }

    public String getNome() {
        return nome;
    }

    public ZonaUrbana getZonaDaEstacao(){
        return zonaDaEstacao;
    }

    public void atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        this.caminhaoGrandeEsperando = caminhao;
        this.temCaminhaoGrandeEsperando = true;
        this.tempoEsperaCaminhaoGrande = 0;
    }
}