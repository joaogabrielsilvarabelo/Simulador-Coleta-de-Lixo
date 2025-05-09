import simulacao.InterfaceSimulador;
import simulacao.Simulador;
import zonas.ZonaUrbana;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
       //simulaçao.Simulador simulador = new simulaçao.Simulador();
        //       simulador.iniciar();
        //        try {
        //            Thread.sleep(3000); // espera 3000 milissegundos (3 segundos)
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //        simulador.gravar("teste.json");
        //        try {
        //            Thread.sleep(1000); // espera 3000 milissegundos (3 segundos)
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //        simulador.encerrar();



        //        FilaCircular filaCircular =  new FilaCircular<>();
        //        filaCircular.enfileirar("JON4S23");
        //        filaCircular.enfileirar("LOL5Z17");
        //        filaCircular.enfileirar("XYZ8J78");
        //        System.out.println("O tamanho da fila é:" + filaCircular.getTamanho());
        //        System.out.println("A fila é:");
        //        filaCircular.imprimirFila();
        //        filaCircular.remover();
        //        System.out.println("O tamanho da fila após a remoção é:" + filaCircular.getTamanho());
        //        System.out.println("A fila após a remoção é");
        //        filaCircular.imprimirFila();

//          ZonaUrbana norte = new ZonaUrbana(1, 4, 10);
//            System.out.println("A zona é:" + norte.getNome());
//            norte.gerarLixo();
//            System.out.println("A variação de pico é:" + norte.getVariacaoPico());
//            System.out.println("A variação normal é:" + norte.getVariacaoNormal());


       Simulador simulador = new Simulador();
       InterfaceSimulador interfaceCLI = new InterfaceSimulador(simulador);
       interfaceCLI.iniciar();


        //ZonaUrbana norte = new ZonaUrbana("Norte");
        //norte.gerarLixo();
        //norte.getLixoAcumulado();


        //Teste placas e capacidade
        //CaminhaoPequeno Primeiro = new CaminhaoPequeno(4);
        //        System.out.println("A capacidade é:" + Primeiro.getCapacidade());
        //        System.out.println("A placa é:" + Primeiro.getPlaca());
        //        CaminhaoPequeno Segundo = new CaminhaoPequeno(2, "JON4S23");
        //        System.out.println("------------------------");
        //        System.out.println("A capacidade é:" + Segundo.getCapacidade());
        //        System.out.println("A placa é:" + Segundo.getPlaca());
        //        CaminhaoPequeno Erro = new CaminhaoPequeno(1, "ERRO1234");
        //        System.out.println("------------------------");
        //        System.out.println("A capacidade é:" + Erro.getCapacidade());
        //        System.out.println("A placa é:" + Erro.getPlaca());
        //        CaminhaoGrande Grande1 = new CaminhaoGrande();
        //        System.out.println("------------------------");
        //        System.out.println("A placa é:" + Grande1.getPlaca());
        //        CaminhaoGrande Grande2 = new CaminhaoGrande("LOL5Z17");
        //        System.out.println("------------------------");
        //        System.out.println("A placa é:" + Grande2.getPlaca());
        //        CaminhaoGrande ErroGrande = new CaminhaoGrande("ERROO0O");
        //        System.out.println("------------------------");
        //        System.out.println("A placa é:" + ErroGrande.getPlaca());


        // Este exemplo simples apenas inicia a simulação.
        // Os alunos devem integrar zonas, caminhões e estações aqui.
    }
}