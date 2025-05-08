package estacoes;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import estruturas.Fila;
import estruturas.Lista;

public class EstacaoTransferencia {

    private String nome;
    private int lixoArmazenado;
    private Fila<CaminhaoPequeno> filaPequenos;
    private Lista listaGrandes;
    private int esperaMaxPequenos;
    private int esperaTotalPequenos;
    private CaminhaoGrande caminhaoGrandeEsperando;
    private boolean TEM_CAMINHAO_GRANDE_ESPERANDO;

    public EstacaoTransferencia(String nome) {
        this.nome = nome;
        this.lixoArmazenado = 0;
        this.filaPequenos = new Fila();
        this.listaGrandes = new Lista();
        this.esperaTotalPequenos = 0;
    }

    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        filaPequenos.enfileirar(caminhao);
        if (filaPequenos.getTamanho() == 1){
            this.esperaTotalPequenos = 0;
        }
    }
    
    public Object atenderFila () {
        if (filaPequenos.estaVazia()) {
            esperaTotalPequenos = 0;
            return null;
        }
        esperaTotalPequenos++;
        if (!TEM_CAMINHAO_GRANDE_ESPERANDO) {
            return null;
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

            esperaTotalPequenos = 0;
        }
    }

    public boolean temCaminhaoGrandeFila(){
        if (caminhaoGrandeEsperando == null){
            return TEM_CAMINHAO_GRANDE_ESPERANDO = false;
        }
        return TEM_CAMINHAO_GRANDE_ESPERANDO = true;
    }

    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        caminhao.carregar(lixoArmazenado);
        System.out.println("Estação " + nome + " carregou caminhão grande com " + lixoArmazenado + "kg.");
        lixoArmazenado = 0;
    }
//        filaPequenos.enfileirar(caminhao.getPlaca());
//        int descarregado = caminhao.descarregar();
//        lixoArmazenado += descarregado;
//        System.out.println("Estação " + nome + " recebeu " + descarregado +
//                "kg de lixo do caminhão " + caminhao.getPlaca());
    public void adicionarCaminhaoGrande(CaminhaoGrande grande) {
        listaGrandes.adicionar(grande);
    }
}