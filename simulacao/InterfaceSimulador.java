package simulacao;

import caminhoes.CaminhaoPequeno;
import estacoes.EstacaoTransferencia;
import estruturas.Lista;
import zonas.ZonaUrbana;

import java.io.IOException;
import java.util.Scanner;

public class InterfaceSimulador {
    private Simulador simulador;
    private Scanner scanner;
    private Lista<CaminhaoPequeno> caminhoesPequenos;
    private Lista<EstacaoTransferencia> estacoes;
    private Lista<ZonaUrbana> zonas;

    public InterfaceSimulador(Simulador simulador) {
        this.simulador = simulador;
        this.scanner = new Scanner(System.in);
        this.caminhoesPequenos = new Lista<>();
        this.estacoes = new Lista<>();
        this.zonas = new Lista<>();
    }

    public void iniciar() {
        configurarSimulador();
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
            int opcao;
            try {
                opcao = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Entrada inválida! Digite um número.");
                scanner.nextLine();
                continue;
            }

            switch (opcao) {
                case 1:
                    simulador.iniciar();
                    break;
                case 2:
                    simulador.pausar();
                    break;
                case 3:
                    simulador.continuarSimulacao();
                    break;
                case 4:
                    simulador.encerrar();
                    break;
                case 5:
                    simulador.getEstatisticas().imprimirRelatorio();
                    break;
                case 6:
                    System.out.print("Nome do arquivo (ex: relatorio.txt): ");
                    String arquivo = scanner.nextLine();
                    try {
                        simulador.getEstatisticas().salvarRelatorio(arquivo);
                        System.out.println("Relatório salvo em " + arquivo);
                    } catch (IOException e) {
                        System.out.println("Erro ao salvar relatório: " + e.getMessage());
                    }
                    break;
                case 7:
                    simulador.encerrar();
                    System.out.println("Saindo...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void configurarSimulador() {
        System.out.println("\n--- Configuração da Simulação ---");
        System.out.print("Quantos caminhões de 2t? ");
        int qtd2t = lerInteiro();
        System.out.print("Quantos caminhões de 4t? ");
        int qtd4t = lerInteiro();
        System.out.print("Quantos caminhões de 8t? ");
        int qtd8t = lerInteiro();
        System.out.print("Quantos caminhões de 10t? ");
        int qtd10t = lerInteiro();

        for (int i = 0; i < qtd2t; i++) caminhoesPequenos.adicionar(new CaminhaoPequeno(1, 10, null));
        for (int i = 0; i < qtd4t; i++) caminhoesPequenos.adicionar(new CaminhaoPequeno(2, 10, null));
        for (int i = 0; i < qtd8t; i++) caminhoesPequenos.adicionar(new CaminhaoPequeno(3, 10, null));
        for (int i = 0; i < qtd10t; i++) caminhoesPequenos.adicionar(new CaminhaoPequeno(4, 10, null));

        System.out.print("Tolerância de espera dos caminhões grandes (min): ");
        int tolerancia = lerInteiro();
        System.out.print("Tempo máximo de espera nas estações (min): ");
        int tempoMaxEspera = lerInteiro();

        zonas.adicionar(new ZonaUrbana(1, 100, 500));
        zonas.adicionar(new ZonaUrbana(2, 100, 500));
        zonas.adicionar(new ZonaUrbana(3, 100, 500));
        zonas.adicionar(new ZonaUrbana(4, 100, 500));
        zonas.adicionar(new ZonaUrbana(5, 100, 500));

        estacoes.adicionar(new EstacaoTransferencia("Estação A", tempoMaxEspera));
        estacoes.adicionar(new EstacaoTransferencia("Estação B", tempoMaxEspera));

        simulador.setListaCaminhoesPequenos(caminhoesPequenos);
        simulador.setListaZonas(zonas);
        simulador.setListaEstacoes(estacoes);
        simulador.setToleranciaCaminhoesGrandes(tolerancia);
    }

    private int lerInteiro() {
        while (true) {
            try {
                int valor = scanner.nextInt();
                scanner.nextLine();
                if (valor >= 0) return valor;
                System.out.print("Digite um número não-negativo: ");
            } catch (Exception e) {
                System.out.print("Entrada inválida! Digite um número: ");
                scanner.nextLine();
            }
        }
    }
}
