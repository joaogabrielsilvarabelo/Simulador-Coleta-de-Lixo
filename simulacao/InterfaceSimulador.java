package simulacao;

import zonas.ZonaUrbana;
import java.io.IOException;
import java.util.Scanner;

public class InterfaceSimulador {
    private final Simulador simulador;
    private final Scanner scanner;

    // Códigos ANSI para colorir a saída
    private static final String RESET = "\u001B[0m";
    private static final String AZUL = "\u001B[34m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARELO = "\u001B[33m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String CIANO = "\u001B[36m";

    public InterfaceSimulador(Simulador simulador) {
        this.simulador = simulador;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        configurarSimulador();
        exibirMenu();
    }

    private void exibirMenu() {
        mostrarMenuCompleto(); // Mostra o menu apenas no início
        while (true) {
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
                System.out.println(VERMELHO + "Entrada inválida! Digite um número entre 1 e 7." + RESET);
                return -1;
            }
        }
    }

    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> {
                System.out.println(VERDE + "\nSimulação iniciada.\n" + RESET);
                simulador.iniciar();
            }
            case 2 -> {
                simulador.pausar();
                mostrarMenuCompleto();
            }
            case 3 -> {
                simulador.continuarSimulacao();
            }
            case 4 -> {
                simulador.encerrar();
                mostrarMenuCompleto();
            }
            case 5 -> {
                simulador.getEstatisticas().imprimirRelatorio();
                mostrarMenuCompleto();
            }
            case 6 -> {
                salvarRelatorio();
                mostrarMenuCompleto();
            }
            case 7 -> sair();
            default -> {
                System.out.println(VERMELHO + "Opção inválida!" + RESET);
                mostrarMenuCompleto();
            }
        }
    }

    private void mostrarMenuCompleto() {
        System.out.println(CIANO + "\n=== Simulador de Coleta de Lixo ===" + RESET);
        System.out.println("1. Iniciar simulação");
        System.out.println("2. Pausar simulação");
        System.out.println("3. Continuar simulação");
        System.out.println("4. Encerrar simulação");
        System.out.println("5. Imprimir relatório");
        System.out.println("6. Salvar relatório em arquivo");
        System.out.println("7. Sair");
        System.out.print(AMARELO + "Escolha uma opção: " + RESET);
        System.out.flush();
    }

    private void salvarRelatorio() {
        System.out.print("Nome do arquivo (ex: relatorio.txt): ");
        String arquivo = scanner.nextLine();
        try {
            simulador.getEstatisticas().salvarRelatorio(arquivo);
            System.out.println(VERDE + "Relatório salvo em " + arquivo + RESET);
        } catch (IOException e) {
            System.out.println(VERMELHO + "Erro ao salvar relatório: " + e.getMessage() + RESET);
        }
    }

    private void sair() {
        simulador.encerrar();
        System.out.println(AZUL + "Saindo..." + RESET);
        scanner.close();
        System.exit(0);
    }

    private void configurarSimulador() {
        System.out.println(CIANO + "\n--- Configuração da Simulação ---" + RESET);
        int qtd2t = lerQuantidade("Quantos caminhões de 2t? ");
        int qtd4t = lerQuantidade("Quantos caminhões de 4t? ");
        int qtd8t = lerQuantidade("Quantos caminhões de 8t? ");
        int qtd10t = lerQuantidade("Quantos caminhões de 10t? ");
        int tolerancia = lerQuantidade("Tolerância de espera dos caminhões grandes (min): ");
        int tempoMaxEspera = lerQuantidade("Tempo máximo de espera nas estações (min): ");
        // Configura o modo de log
        LoggerSimulacao.ModoLog modoLog = lerModoLog("Modo de log (1: Normal, 2: Debug): ");
        LoggerSimulacao.setModoLog(modoLog);

        Simulador.inicializarZonas();
        Simulador.gerarLixoZonas();
        ZonaUrbana zonaEstacaoA = lerZona("Qual é a zona que a estação A estará? (1: Norte; 2: Sul; 3: Leste; 4: Sudeste; 5: Centro): ");
        ZonaUrbana zonaEstacaoB = lerZona("Qual é a zona que a estação B estará? (1: Norte; 2: Sul; 3: Leste; 4: Sudeste; 5: Centro): ");
        Simulador.inicializarCaminhoes(qtd2t, qtd4t, qtd8t, qtd10t);
        System.out.println(VERDE + "Inicializados " + Simulador.getCaminhoesPequenos().getTamanho() + " caminhões." + RESET);
        Simulador.inicializarEstacoes(tempoMaxEspera, zonaEstacaoA, zonaEstacaoB);
        Simulador.configurarSimuladorParams(tolerancia);
    }

    private int lerQuantidade(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            synchronized (System.in) {
                try {
                    int valor = Integer.parseInt(scanner.nextLine().trim());
                    if (valor >= 0) return valor;
                    System.out.println(VERMELHO + "Digite um número não-negativo!" + RESET);
                } catch (NumberFormatException e) {
                    System.out.println(VERMELHO + "Entrada inválida! Digite um número!" + RESET);
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
                    if (zona >= 1 && zona <= 5) {
                        return Simulador.getZonas().obter(zona - 1);
                    }
                    System.out.println(VERMELHO + "Digite um número de zona válido!" + RESET);
                } catch (NumberFormatException e) {
                    System.out.println(VERMELHO + "Entrada inválida! Digite um número!" + RESET);
                }
            }
        }
    }

    private LoggerSimulacao.ModoLog lerModoLog(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            synchronized (System.in) {
                try {
                    int modo = Integer.parseInt(scanner.nextLine().trim());
                    if (modo == 1) return LoggerSimulacao.ModoLog.NORMAL;
                    if (modo == 2) return LoggerSimulacao.ModoLog.DEBUG;
                    System.out.println(VERMELHO + "Digite 1 para Normal ou 2 para Debug!" + RESET);
                } catch (NumberFormatException e) {
                    System.out.println(VERMELHO + "Entrada inválida! Digite um número!" + RESET);
                }
            }
        }
    }
}

