package simulacao;

import zonas.ZonaUrbana;
import java.io.IOException;
import java.util.Scanner;

public class InterfaceSimulador {
    private final Simulador simulador;
    private final Scanner scanner;

    public InterfaceSimulador(Simulador simulador) {
        this.simulador = simulador;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        configurarSimulador();
        exibirMenu();
    }

    private void exibirMenu() {
        mostrarMenuCompleto();
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
                LoggerSimulacao.log("ERRO", "Entrada inválida! Digite um número entre 1 e 7.");
                if (LoggerSimulacao.ModoLog.DEBUG == LoggerSimulacao.getModoLog()) {
                    LoggerSimulacao.log("INFO", String.format("Entrada inválida recebida: %s", e.getMessage()));
                }
                return -1;
            }
        }
    }

    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> {
                LoggerSimulacao.log("INFO", "Simulação iniciada.");
                simulador.iniciar();
            }
            case 2 -> {
                simulador.pausar();
                mostrarMenuCompleto();
            }
            case 3 -> simulador.continuarSimulacao();
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
                LoggerSimulacao.log("ERRO", "Opção inválida!");
                mostrarMenuCompleto();
            }
        }
    }

    private void mostrarMenuCompleto() {
        LoggerSimulacao.log("CONFIG", "=== Simulador de Coleta de Lixo ===");
        LoggerSimulacao.log("CONFIG", "1. Iniciar simulação");
        LoggerSimulacao.log("CONFIG", "2. Pausar simulação");
        LoggerSimulacao.log("CONFIG", "3. Continuar simulação");
        LoggerSimulacao.log("CONFIG", "4. Encerrar simulação");
        LoggerSimulacao.log("CONFIG", "5. Imprimir relatório");
        LoggerSimulacao.log("CONFIG", "6. Salvar relatório em arquivo");
        LoggerSimulacao.log("CONFIG", "7. Sair");
        LoggerSimulacao.log("CONFIG", "Escolha uma opção: ");
    }

    private void salvarRelatorio() {
        LoggerSimulacao.log("INFO", "Nome do arquivo (ex: relatorio.txt): ");
        String arquivo = scanner.nextLine();
        try {
            simulador.getEstatisticas().salvarRelatorio(arquivo);
            LoggerSimulacao.log("INFO", String.format("Relatório salvo em %s", arquivo));
        } catch (IOException e) {
            LoggerSimulacao.log("ERRO", String.format("Erro ao salvar relatório: %s", e.getMessage()));
        }
    }

    private void sair() {
        simulador.encerrar();
        LoggerSimulacao.log("INFO", "Saindo...");
        scanner.close();
        System.exit(0);
    }

    private void configurarSimulador() {
        LoggerSimulacao.log("CONFIG", "--- Configuração da Simulação ---");
        // Prompt for log file name
        LoggerSimulacao.log("CONFIG", "Nome do arquivo de log (ex: eventos.log): ");
        String logFileName = scanner.nextLine().trim();
        if (logFileName.isEmpty()) {
            logFileName = "eventos.log"; // Default log file name
        }
        LoggerSimulacao.inicializarLogArquivo(logFileName);
        int qtd2t = lerQuantidade("Quantos caminhões de 2t? ");
        int qtd4t = lerQuantidade("Quantos caminhões de 4t? ");
        int qtd8t = lerQuantidade("Quantos caminhões de 8t? ");
        int qtd10t = lerQuantidade("Quantos caminhões de 10t? ");
        int tolerancia = lerQuantidade("Tolerância de espera dos caminhões grandes (min): ");
        int tempoMaxEspera = lerQuantidade("Tempo máximo de espera nas estações (min): ");
        int limiteViagens = lerQuantidade("Quantidade máxima de viagens diárias: ");
        LoggerSimulacao.ModoLog modoLog = lerModoLog();
        LoggerSimulacao.setModoLog(modoLog);

        // Configuração de intervalos de lixo por zona
        int[][] intervalosLixo = new int[5][2];
        String[] zonasNomes = {"Norte", "Sul", "Leste", "Sudeste", "Centro"};
        int opcaoPadrao = lerOpcaoPadrao();
        if (opcaoPadrao == 1) {
            intervalosLixo[0][0] = 2900 * 24; // Norte: 69600 kg
            intervalosLixo[0][1] = 5000 * 24; // Norte: 120000 kg
            intervalosLixo[1][0] = 3500 * 24; // Sul: 84000 kg
            intervalosLixo[1][1] = 6500 * 24; // Sul: 156000 kg
            intervalosLixo[2][0] = 3900 * 24; // Leste: 93600 kg
            intervalosLixo[2][1] = 6800 * 24; // Leste: 163200 kg
            intervalosLixo[3][0] = 2800 * 24; // Sudeste: 67200 kg
            intervalosLixo[3][1] = 5500 * 24; // Sudeste: 132000 kg
            intervalosLixo[4][0] = 3500 * 24; // Centro: 84000 kg
            intervalosLixo[4][1] = 6000 * 24; // Centro: 144000 kg
            LoggerSimulacao.log("CONFIG", "Intervalos padrão aplicados:");
        } else {
            for (int i = 0; i < 5; i++) {
                LoggerSimulacao.log("CONFIG", String.format("Configurando geração de lixo para zona %s:", zonasNomes[i]));
                intervalosLixo[i][0] = lerQuantidade("  Intervalo mínimo de lixo diário (kg): ");
                intervalosLixo[i][1] = lerQuantidade("  Intervalo máximo de lixo diário (kg): ");
                if (intervalosLixo[i][1] < intervalosLixo[i][0]) {
                    LoggerSimulacao.log("ERRO", "Máximo deve ser maior ou igual ao mínimo! Usando valores padrão.");
                    intervalosLixo[i][0] = (i == 0 ? 2900 : i == 1 ? 3500 : i == 2 ? 3900 : i == 3 ? 2800 : 3500) * 24;
                    intervalosLixo[i][1] = (i == 0 ? 5000 : i == 1 ? 6500 : i == 2 ? 6800 : i == 3 ? 5500 : 6000) * 24;
                }
            }
        }

        Simulador.inicializarZonas(intervalosLixo);
        Simulador.gerarLixoZonas();
        ZonaUrbana zonaEstacaoA = lerZona("Qual é a zona que a estação A estará? (1: Norte; 2: Sul; 3: Leste; 4: Sudeste; 5: Centro): ");
        ZonaUrbana zonaEstacaoB = lerZona("Qual é a zona que a estação B estará? (1: Norte; 2: Sul; 3: Leste; 4: Sudeste; 5: Centro): ");
        ZonaUrbana zonaAterro = lerZona("Qual é a zona que o aterro estará? (1: Norte; 2: Sul; 3: Leste; 4: Sudeste; 5: Centro): ");
        Simulador.inicializarCaminhoes(qtd2t, qtd4t, qtd8t, qtd10t, limiteViagens);
        LoggerSimulacao.log("CONFIG", String.format("Inicializados %d caminhões.", Simulador.getCaminhoesPequenos().getTamanho()));
        Simulador.inicializarEstacoes(tempoMaxEspera, zonaEstacaoA, zonaEstacaoB);
        Simulador.inicializarAterro(zonaAterro);
        Simulador.configurarSimuladorParams(tolerancia);
    }

    private int lerQuantidade(String mensagem) {
        while (true) {
            LoggerSimulacao.log("CONFIG", mensagem);
            synchronized (System.in) {
                try {
                    int valor = Integer.parseInt(scanner.nextLine().trim());
                    if (valor >= 0) return valor;
                    LoggerSimulacao.log("ERRO", "Digite um número não-negativo!");
                } catch (NumberFormatException e) {
                    LoggerSimulacao.log("ERRO", "Entrada inválida! Digite um número!");
                }
            }
        }
    }

    private ZonaUrbana lerZona(String mensagem) {
        while (true) {
            LoggerSimulacao.log("CONFIG", mensagem);
            synchronized (System.in) {
                try {
                    int zona = Integer.parseInt(scanner.nextLine().trim());
                    if (zona >= 1 && zona <= 5) {
                        return Simulador.getZonas().obter(zona - 1);
                    }
                    LoggerSimulacao.log("ERRO", "Digite um número de zona válido!");
                } catch (NumberFormatException e) {
                    LoggerSimulacao.log("ERRO", "Entrada inválida! Digite um número!");
                }
            }
        }
    }

    private LoggerSimulacao.ModoLog lerModoLog() {
        while (true) {
            LoggerSimulacao.log("CONFIG", "Modo de log (1: Normal, 2: Debug): ");
            synchronized (System.in) {
                try {
                    int modo = Integer.parseInt(scanner.nextLine().trim());
                    if (modo == 1) return LoggerSimulacao.ModoLog.NORMAL;
                    if (modo == 2) return LoggerSimulacao.ModoLog.DEBUG;
                    LoggerSimulacao.log("ERRO", "Digite 1 para Normal ou 2 para Debug!");
                } catch (NumberFormatException e) {
                    LoggerSimulacao.log("ERRO", "Entrada inválida! Digite um número!");
                }
            }
        }
    }

    private int lerOpcaoPadrao() {
        while (true) {
            LoggerSimulacao.log("CONFIG", "Usar intervalos de lixo padrão para todas as zonas? (1: Sim, 2: Não): ");
            synchronized (System.in) {
                try {
                    int opcao = Integer.parseInt(scanner.nextLine().trim());
                    if (opcao == 1 || opcao == 2) return opcao;
                    LoggerSimulacao.log("ERRO", "Digite 1 para Sim ou 2 para Não!");
                } catch (NumberFormatException e) {
                    LoggerSimulacao.log("ERRO", "Entrada inválida! Digite um número!");
                }
            }
        }
    }
}


