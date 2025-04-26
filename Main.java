import caminhoes.CaminhaoPequenoPadrao;
import zonas.ZonaUrbana;

public class Main {
    public static void main(String[] args) {
        //Simulador simulador = new Simulador();
        //simulador.iniciar();
        ZonaUrbana norte = new ZonaUrbana("Norte");
        norte.gerarLixo();
        norte.getLixoAcumulado();




        // Este exemplo simples apenas inicia a simulação.
        // Os alunos devem integrar zonas, caminhões e estações aqui.
    }
}