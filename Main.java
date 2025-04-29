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


        //Teste rapidao



        // Este exemplo simples apenas inicia a simulação.
        // Os alunos devem integrar zonas, caminhões e estações aqui.
    }
}