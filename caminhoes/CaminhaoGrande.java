package caminhoes;

import estacoes.EstacaoTransferencia;
import simulacao.LoggerSimulacao;
import simulacao.Simulador;

// Representa um caminhão grande que transporta lixo para o aterro
public class CaminhaoGrande {
    private static final int TEMPO_DESCARREGAMENTO = 30; // Tempo fixo para descarregar no aterro (minutos)
    private String placa;
    private int capacidade;
    private int cargaAtual;
    private int toleranciaEspera;
    private int status; // 0: ESPERANDO, 1: EM_VIAGEM_PARA_ATERRO, 2: DESCARREGANDO, 3: RETORNANDO
    private int tempoViagemRestante; // Tempo restante para viagem ou descarregamento
    private EstacaoTransferencia estacaoOrigem; // Estação à qual o caminhão está associado
    private EstacaoTransferencia estacaoDestino;// Estação para onde retorna, se redirecionado

    public CaminhaoGrande(int toleranciaEspera) {
        this.placa = Placa.gerarPlaca();
        this.capacidade = 20000;
        this.toleranciaEspera = toleranciaEspera;
        this.cargaAtual = 0;
        this.status = 0; // ESPERANDO
        this.tempoViagemRestante = 0;
        this.estacaoOrigem = null;
        this.estacaoDestino = null;
    }

    // Carrega lixo no caminhão
    public void carregar(int quantidade) {
        cargaAtual = Math.min(cargaAtual + quantidade, capacidade);
    }

    // Descarrega o lixo no aterro e registra nas estatísticas
    public void descarregar() {
        if (status == 2) { // Apenas descarrega no status DESCARREGANDO
            Simulador.getEstatisticas().registrarLixoAterro(cargaAtual);
            LoggerSimulacao.log("DESCARGA", String.format("Caminhão grande %s descarregou %dkg no aterro", placa, cargaAtual));
            cargaAtual = 0;
        }
    }

    // Define a estação de origem
    public void setEstacaoOrigem(EstacaoTransferencia estacao) {
        this.estacaoOrigem = estacao;
    }

    // Define a estação de destino para retorno
    public void setEstacaoDestino(EstacaoTransferencia estacao) {
        this.estacaoDestino = estacao;
    }

    // Inicia viagem para o aterro
    public void iniciarViagemParaAterro(int tempoViagem) {
        status = 1; // EM_VIAGEM_PARA_ATERRO
        tempoViagemRestante = tempoViagem;
        LoggerSimulacao.log("VIAGEM", String.format("Caminhão grande %s iniciando viagem para o aterro (tempo: %dmin)", placa, tempoViagem));
    }

    // Inicia descarregamento no aterro
    public void iniciarDescarregamento() {
        status = 2; // DESCARREGANDO
        tempoViagemRestante = TEMPO_DESCARREGAMENTO;
        LoggerSimulacao.log("DESCARGA", String.format("Caminhão grande %s iniciando descarregamento no aterro", placa));
    }

    // Inicia carregamento da carga do caminhão pequeno
    public void iniciarCarregamento() {
        status = 4; // CARREGANDO_LIXO
        tempoViagemRestante = TEMPO_DESCARREGAMENTO;
        LoggerSimulacao.log("DESCARGA", String.format("Caminhão grande %s iniciando descarregamento no aterro", placa));
    }

    // Inicia retorno para a estação
    public void iniciarRetorno(int tempoViagem) {
        status = 3; // RETORNANDO
        tempoViagemRestante = tempoViagem;
        LoggerSimulacao.log("VIAGEM", String.format("Caminhão grande %s retornando para %s (tempo: %dmin)", placa, estacaoDestino.getNome(), tempoViagem));
    }

    // Atualiza o status do caminhão com base no tempo
    public boolean atualizarEstado() {
        if (status == 0) {
            return false; // Sem ação se esperando
        }
        tempoViagemRestante--;
        if (tempoViagemRestante <= 0) {
            if (status == 1) { // Chegou ao aterro
                LoggerSimulacao.log("CHEGADA", String.format("Caminhão grande %s chegou ao aterro", placa));
                iniciarDescarregamento();
                return false;
            } else if (status == 2) { // Terminou descarregamento
                descarregar();
                // Calcula tempo de retorno
                int tempoRetorno = DistribuicaoCaminhoes.calcularTempoViagem(Simulador.getZonaAterro(), estacaoDestino.getZonaDaEstacao());
                iniciarRetorno(tempoRetorno);
                return false;
            } else if (status == 3) { // Chegou à estação
                status = 0; // ESPERANDO
                estacaoDestino.atribuirCaminhaoGrande(this);
                LoggerSimulacao.log("CHEGADA", String.format("Caminhão grande %s chegou à estação %s", placa, estacaoDestino.getNome()));
                return true; // Pronto para ser reutilizado
            }
            else if (status == 4){

                LoggerSimulacao.log("COLETA", String.format("Caminhão grande %s recebeu %s", placa, estacaoDestino.getNome()));
            }
        }
        return false;
    }

    // Getters
    public String getPlaca() { return placa; }
    public int getCapacidade() { return capacidade; }
    public int getCargaAtual() { return cargaAtual; }
    public int getToleranciaEspera() { return toleranciaEspera; }
    public int getEstado() { return status; }
    public EstacaoTransferencia getEstacaoOrigem() { return estacaoOrigem; }
    public EstacaoTransferencia getEstacaoDestino(){ return estacaoDestino; }
}


