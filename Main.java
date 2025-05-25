import simulacao.InterfaceSimulador;
import simulacao.LoggerSimulacao;
import simulacao.Simulador;

public class Main {
    public static void main(String[] args) {
        try {
            Simulador simulador = new Simulador();
            InterfaceSimulador interfaceCLI = new InterfaceSimulador(simulador);
            interfaceCLI.iniciar();
        } catch (Exception e) {
            LoggerSimulacao.log("ERRO","Erro ao iniciar o simulador: " + e.getMessage());
        }
    }
}