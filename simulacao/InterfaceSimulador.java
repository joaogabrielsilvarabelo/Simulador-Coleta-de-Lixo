package simulacao;

import caminhoes.CaminhaoPequeno;
import estacoes.EstacaoTransferencia;
import estruturas.Lista;
import zonas.ZonaUrbana;

import java.io.IOException;
import java.util.Scanner;

public class InterfaceSimulador {
    private final Simulador simulador;
    private final Scanner scanner;
    private final Lista<CaminhaoPequeno> caminhoesPequenos;
    private final Lista<EstacaoTransferencia> estacoes;
    private final Lista<ZonaUrbana> zonas;
    private static int caminhoesPorZona;

    public InterfaceSimulador(Simulador simulador) {
        this.simulador = simulador;
        this.scanner = new Scanner(System.in);
        this.caminhoesPequenos = new Lista<>();
        this.estacoes = new Lista<>();
        this.zonas = new Lista<>();
        caminhoesPorZona = 0;
    }

    public void iniciar() {
        configurarSimulador();
        exibirMenu();
    }

    private void exibirMenu() {
        while (true) {
            System.out.println("\n=== Simulador de Coleta de Lixo ===");
            System.out.println("1. Iniciar simulação");
            System.out.println("2. Pausar simulação");
            System.out.println("3. Continuar simulação");
            System.out.println("4. Encerrar simulação");
            System.out.println("5. Imprimir relatório");
            System.out.println("6. Salvar relatório em arquivo");
            System.out.println("7. Sair");
            System.out.print("Escolha uma opção: ");
            System.out.flush();

            int opcao = lerOpcao();
            processarOpcao(opcao);
        }
    }

    private int lerOpcao() {
        synchronized (System.in) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida! Digite um número entre 1 e 7.");
                return -1;
            }
        }
    }

    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> simulador.iniciar();
            case 2 -> simulador.pausar();
            case 3 -> simulador.continuarSimulacao();
            case 4 -> simulador.encerrar();
            case 5 -> simulador.getEstatisticas().imprimirRelatorio();
            case 6 -> salvarRelatorio();
            case 7 -> sair();
            default -> System.out.println("Opção inválida!");
        }
    }

    private void salvarRelatorio() {
        System.out.print("Nome do arquivo (ex: relatorio.txt): ");
        String arquivo = scanner.nextLine();
        try {
            simulador.getEstatisticas().salvarRelatorio(arquivo);
            System.out.println("Relatório salvo em " + arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao salvar relatório: " + e.getMessage());
        }
    }

    private void sair() {
        simulador.encerrar();
        System.out.println("Saindo...");
        scanner.close();
        System.exit(0);
    }

    private void configurarSimulador() {
        System.out.println("\n--- Configuração da Simulação ---");
        int qtd2t = lerQuantidade("Quantos caminhões de 2t? ");
        int qtd4t = lerQuantidade("Quantos caminhões de 4t? ");
        int qtd8t = lerQuantidade("Quantos caminhões de 8t? ");
        int qtd10t = lerQuantidade("Quantos caminhões de 10t? ");
        int tolerancia = lerQuantidade("Tolerância de espera dos caminhões grandes (min): ");
        int tempoMaxEspera = lerQuantidade("Tempo máximo de espera nas estações (min): ");

        inicializarZonas();
        ZonaUrbana zonaEstacaoA = lerZona("Qual é a zona que a estação A estará? (1: Norte; 2: Sul; 3: Leste; 4: Sudeste; 5: Centro): ");
        ZonaUrbana zonaEstacaoB = lerZona("Qual é a zona que a estação B estará? (1: Norte; 2: Sul; 3: Leste; 4: Sudeste; 5: Centro): ");
        inicializarCaminhoes(qtd2t, qtd4t, qtd8t, qtd10t);
        inicializarEstacoes(tempoMaxEspera, zonaEstacaoA, zonaEstacaoB);
        configurarSimuladorParams(tolerancia);
    }

    private int lerQuantidade(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            synchronized (System.in) {
                try {
                    int valor = Integer.parseInt(scanner.nextLine().trim());
                    if (valor >= 0) return valor;
                    System.out.println("Digite um número não-negativo!");
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida! Digite um número!");
                }
            }
        }
    }

    private ZonaUrbana lerZona(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            synchronized (System.in) {
                try {
                    int zona = Integer.parseInt(scanner.nextLine().trim());
                    if (zona >= 1 && zona <= 5){
                        return zonas.obter(zona);
                    }
                    System.out.println("Digite um número de zona válido!");
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida! Digite um número!");
                }
            }
        }
    }

    private void inicializarZonas() {
        zonas.adicionar(new ZonaUrbana(1, 2900, 5000)); // Norte
        zonas.adicionar(new ZonaUrbana(2, 3500, 6500)); // Sul
        zonas.adicionar(new ZonaUrbana(3, 3900, 6800)); // Leste
        zonas.adicionar(new ZonaUrbana(4, 2800, 5500)); // Sudeste
        zonas.adicionar(new ZonaUrbana(5, 3500, 6000)); // Centro
    }

    private void inicializarCaminhoes(int qtd2t, int qtd4t, int qtd8t, int qtd10t) {
        int totalCaminhoes = qtd2t + qtd4t + qtd8t + qtd10t;
        int TOTAL_ZONAS = 5;
        caminhoesPorZona = totalCaminhoes / TOTAL_ZONAS;
        int extras = totalCaminhoes % TOTAL_ZONAS;

        int[] quantidades = {qtd2t, qtd4t, qtd8t, qtd10t};
        int caminhaoIdx = 0;

        for (int i = 0; i < TOTAL_ZONAS; i++) {
            ZonaUrbana zonaInicial = zonas.obter(i);
            int caminhoesParaZona = caminhoesPorZona + (i < extras ? 1 : 0);

            for (int j = 0; j < caminhoesParaZona && caminhaoIdx < totalCaminhoes; j++) {
                // Escolher tipo de caminhão
                for (int k = 0; k < quantidades.length; k++) {
                    if (quantidades[k] > 0) {
                        CaminhaoPequeno caminhao = new CaminhaoPequeno(k + 1, 10, zonaInicial);
                        caminhao.setEstado(1); // DISPONÍVEL
                        caminhoesPequenos.adicionar(caminhao);
                        quantidades[k]--;
                        caminhaoIdx++;
                        break;
                    }
                }
            }
        }
        System.out.println("Inicializados " + caminhoesPequenos.getTamanho() + " caminhões.");
    }

    private void configurarSimuladorParams(int tolerancia) {
        simulador.setListaCaminhoesPequenos(caminhoesPequenos);
        simulador.setListaZonas(zonas);
        simulador.setListaEstacoes(estacoes);
        simulador.setToleranciaCaminhoesGrandes(tolerancia);
        simulador.inicializarCaminhoesGrandes(estacoes.getTamanho());
    }

    private void inicializarEstacoes(int tempoMaxEspera, ZonaUrbana zonaEstacaoA, ZonaUrbana zonaEstacaoB) {
        estacoes.adicionar(new EstacaoTransferencia("Estação A", tempoMaxEspera, zonaEstacaoA));
        estacoes.adicionar(new EstacaoTransferencia("Estação B", tempoMaxEspera, zonaEstacaoB));
    }

    public static int getCaminhoesPorZona(){
        return caminhoesPorZona;
    }
}
