import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;

public class Main {
    public static void main(String[] args) {
        //Simulador simulador = new Simulador();
        //simulador.iniciar();
        //ZonaUrbana norte = new ZonaUrbana("Norte");
        //norte.gerarLixo();
        //norte.getLixoAcumulado();

        CaminhaoPequeno Primeiro = new CaminhaoPequeno(4);
        System.out.println("A capacidade é:" + Primeiro.getCapacidade());
        System.out.println("A placa é:" + Primeiro.getPlaca());
        CaminhaoPequeno Segundo = new CaminhaoPequeno(2, "JON4S23");
        System.out.println("------------------------");
        System.out.println("A capacidade é:" + Segundo.getCapacidade());
        System.out.println("A placa é:" + Segundo.getPlaca());
        CaminhaoPequeno Erro = new CaminhaoPequeno(1, "ERRO1234");
        System.out.println("------------------------");
        System.out.println("A capacidade é:" + Erro.getCapacidade());
        System.out.println("A placa é:" + Erro.getPlaca());
        CaminhaoGrande Grande1 = new CaminhaoGrande();
        System.out.println("------------------------");
        System.out.println("A placa é:" + Grande1.getPlaca());
        CaminhaoGrande Grande2 = new CaminhaoGrande("LOL5Z17");
        System.out.println("------------------------");
        System.out.println("A placa é:" + Grande2.getPlaca());
        CaminhaoGrande ErroGrande = new CaminhaoGrande("ERROO0O");
        System.out.println("------------------------");
        System.out.println("A placa é:" + ErroGrande.getPlaca());




        // Este exemplo simples apenas inicia a simulação.
        // Os alunos devem integrar zonas, caminhões e estações aqui.
    }
}