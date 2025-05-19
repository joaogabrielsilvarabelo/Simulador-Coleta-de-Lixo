
import simulacao.InterfaceSimulador;
import simulacao.Simulador;

public class Main {
    public static void main(String[] args) {
        try {
            Simulador simulador = new Simulador();
            InterfaceSimulador interfaceCLI = new InterfaceSimulador(simulador);
            interfaceCLI.iniciar();
        } catch (Exception e) {
            log("Erro ao iniciar o simulador: " + e.getMessage());
        }
    }
    private static void log(String mensagem) {
        System.out.println(mensagem);
    }
}